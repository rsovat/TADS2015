/*
 * Copyright 2017 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.mongodb.connection;

import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.mongodb.internal.validator.MappedFieldNameValidator;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonElement;
import org.bson.BsonString;
import org.bson.BsonWriter;
import org.bson.FieldNameValidator;
import org.bson.codecs.EncoderContext;
import org.bson.io.BsonOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.ReadPreference.primary;
import static com.mongodb.assertions.Assertions.isTrue;
import static com.mongodb.connection.BsonWriterHelper.writePayload;

/**
 * A command message that uses OP_MSG or OP_QUERY to send the command.
 */
final class CommandMessage extends RequestMessage {
    private final MongoNamespace namespace;
    private final BsonDocument command;
    private final FieldNameValidator commandFieldNameValidator;
    private final ReadPreference readPreference;
    private final SplittablePayload payload;
    private final FieldNameValidator payloadFieldNameValidator;
    private final boolean responseExpected;

    CommandMessage(final MongoNamespace namespace, final BsonDocument command, final FieldNameValidator commandFieldNameValidator,
                   final ReadPreference readPreference, final MessageSettings settings) {
        this(namespace, command, commandFieldNameValidator, readPreference, settings, true, null, null);
    }

    CommandMessage(final MongoNamespace namespace, final BsonDocument command, final FieldNameValidator commandFieldNameValidator,
                   final ReadPreference readPreference, final MessageSettings settings, final boolean responseExpected,
                   final SplittablePayload payload, final FieldNameValidator payloadFieldNameValidator) {
        super(namespace.getFullName(), getOpCode(settings), settings);
        this.namespace = namespace;
        this.command = command;
        this.commandFieldNameValidator = commandFieldNameValidator;
        this.responseExpected = responseExpected;
        this.payload = payload;
        this.payloadFieldNameValidator = payloadFieldNameValidator;
        this.readPreference = readPreference;
    }

    boolean containsPayload() {
        return payload != null;
    }

    boolean isResponseExpected() {
        isTrue("The message must be encoded before determining if a response is expected", getEncodingMetadata() != null);
        return calculateIsResponseExpected();
    }

    ReadPreference getReadPreference() {
        return readPreference;
    }

    @Override
    protected EncodingMetadata encodeMessageBodyWithMetadata(final BsonOutput bsonOutput, final SessionContext sessionContext) {
        int commandStartPosition;
        if (useOpMsg()) {
            int flagPosition = bsonOutput.getPosition();
            bsonOutput.writeInt32(0);   // flag bits
            bsonOutput.writeByte(0);    // payload type
            commandStartPosition = bsonOutput.getPosition();

            addDocument(getCommandToEncode(), bsonOutput, commandFieldNameValidator, getExtraElements(sessionContext));

            if (payload != null) {
                bsonOutput.writeByte(1);          // payload type
                int payloadPosition = bsonOutput.getPosition();
                bsonOutput.writeInt32(0);         // size
                bsonOutput.writeCString(payload.getPayloadName());
                writePayload(new BsonBinaryWriter(bsonOutput, payloadFieldNameValidator), bsonOutput, getSettings(),
                        commandStartPosition, payload);

                int payloadLength = bsonOutput.getPosition() - payloadPosition;
                bsonOutput.writeInt32(payloadPosition, payloadLength);
            }

            // Write the flag bits
            bsonOutput.writeInt32(flagPosition, getFlagBits());
        } else {
            bsonOutput.writeInt32(0);
            bsonOutput.writeCString(namespace.getFullName());
            bsonOutput.writeInt32(0);
            bsonOutput.writeInt32(-1);

            commandStartPosition = bsonOutput.getPosition();

            if (payload == null) {
                addDocument(getCommandToEncode(), bsonOutput, commandFieldNameValidator, null);
            } else {
                addDocumentWithPayload(bsonOutput);
            }
        }
        return new EncodingMetadata(commandStartPosition);
    }

    private FieldNameValidator getPayloadArrayFieldNameValidator() {
        Map<String, FieldNameValidator> rootMap = new HashMap<String, FieldNameValidator>();
        rootMap.put(payload.getPayloadName(), payloadFieldNameValidator);
        return new MappedFieldNameValidator(commandFieldNameValidator, rootMap);
    }

    private void addDocumentWithPayload(final BsonOutput bsonOutput) {
        BsonWriter writer = new BsonBinaryWriter(bsonOutput, getPayloadArrayFieldNameValidator());
        if (payload != null) {
            writer =  new SplittablePayloadBsonWriter(writer, bsonOutput, getSettings(), payload);
        }
        BsonDocument commandToEncode = getCommandToEncode();
        getCodec(commandToEncode).encode(writer, commandToEncode, EncoderContext.builder().build());
    }

    private int getFlagBits() {
        if (calculateIsResponseExpected()) {
            return 0;
        } else {
            return 1 << 1;
        }
    }

    private boolean calculateIsResponseExpected() {
        // If there is another message in the payload require that the response is acknowledged
        if (!responseExpected && useOpMsg() && payload != null && payload.hasAnotherSplit()) {
            return true;
        }
        return responseExpected;
    }

    private boolean useOpMsg() {
        return getOpCode().equals(OpCode.OP_MSG);
    }

    private BsonDocument getCommandToEncode() {
        BsonDocument commandToEncode = command;
        if (!useOpMsg() && !isDefaultReadPreference(getReadPreference())) {
            commandToEncode = new BsonDocument("$query", command).append("$readPreference", getReadPreference().toDocument());
        }
        return commandToEncode;
    }

    private List<BsonElement> getExtraElements(final SessionContext sessionContext) {
        List<BsonElement> extraElements = new ArrayList<BsonElement>();
        extraElements.add(new BsonElement("$db", new BsonString(new MongoNamespace(getCollectionName()).getDatabaseName())));
        if (sessionContext.getClusterTime() != null) {
            extraElements.add(new BsonElement("$clusterTime", sessionContext.getClusterTime()));
        }
        if (sessionContext.hasSession()) {
            extraElements.add(new BsonElement("lsid", sessionContext.getSessionId()));
        }
        if (!isDefaultReadPreference(getReadPreference())) {
            extraElements.add(new BsonElement("$readPreference", getReadPreference().toDocument()));
        }
        return extraElements;
    }

    private boolean isDefaultReadPreference(final ReadPreference readPreference) {
        return readPreference.equals(primary());
    }

    private static OpCode getOpCode(final MessageSettings settings) {
        return isServerVersionAtLeastThreeDotSix(settings) ? OpCode.OP_MSG : OpCode.OP_QUERY;
    }

    private static boolean isServerVersionAtLeastThreeDotSix(final MessageSettings settings) {
        return settings.getServerVersion().compareTo(new ServerVersion(3, 5)) >= 0;
    }

}

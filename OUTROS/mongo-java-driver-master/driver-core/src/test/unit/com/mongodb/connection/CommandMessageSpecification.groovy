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

package com.mongodb.connection

import com.mongodb.MongoNamespace
import com.mongodb.ReadPreference
import com.mongodb.internal.validator.NoOpFieldNameValidator
import org.bson.BsonBinary
import org.bson.BsonDocument
import org.bson.BsonSerializationException
import org.bson.BsonString
import org.bson.BsonTimestamp
import org.bson.ByteBufNIO
import org.bson.codecs.BsonDocumentCodec
import org.bson.io.BasicOutputBuffer
import spock.lang.Specification

import java.nio.ByteBuffer

class CommandMessageSpecification extends Specification {

    def namespace = new MongoNamespace('db.test')
    def command = new BsonDocument('insert', new BsonString('coll'))
    def fieldNameValidator = new NoOpFieldNameValidator()

    def 'should encode command message with OP_MSG'() {
        given:
        def message = new CommandMessage(namespace, command, fieldNameValidator, readPreference,
                MessageSettings.builder().serverVersion(new ServerVersion(3, 6)).build())
        def output = new BasicOutputBuffer()

        when:
        message.encode(output, sessionContext)

        then:
        def byteBuf = new ByteBufNIO(ByteBuffer.wrap(output.toByteArray()))
        def messageHeader = new MessageHeader(byteBuf, 512)
        messageHeader.opCode == OpCode.OP_MSG.value
        messageHeader.requestId == RequestMessage.currentGlobalId - 1
        messageHeader.responseTo == 0

        def expectedCommandDocument = command.clone()
                .append('$db', new BsonString('db'))

        if (sessionContext.clusterTime != null) {
            expectedCommandDocument.append('$clusterTime', sessionContext.clusterTime)
        }
        if (sessionContext.hasSession()) {
            expectedCommandDocument.append('lsid', sessionContext.sessionId)
        }

        if (readPreference != ReadPreference.primary()) {
            expectedCommandDocument.append('$readPreference', readPreference.toDocument())
        }
        getCommandDocument(byteBuf, messageHeader) == expectedCommandDocument

        where:
        [readPreference, sessionContext] << [
                [ReadPreference.primary(), ReadPreference.secondary()],
                [
                        Stub(SessionContext) {
                            hasSession() >> false
                            getClusterTime() >> null
                            getSessionId() >> new BsonDocument('id', new BsonBinary([1, 2, 3] as byte[]))
                        },
                        Stub(SessionContext) {
                            hasSession() >> false
                            getClusterTime() >> new BsonDocument('clusterTime', new BsonTimestamp(42, 1))
                        },
                        Stub(SessionContext) {
                            hasSession() >> true
                            getClusterTime() >> null
                            getSessionId() >> new BsonDocument('id', new BsonBinary([1, 2, 3] as byte[]))
                        },
                        Stub(SessionContext) {
                            hasSession() >> true
                            getClusterTime() >> new BsonDocument('clusterTime', new BsonTimestamp(42, 1))
                            getSessionId() >> new BsonDocument('id', new BsonBinary([1, 2, 3] as byte[]))
                        }
                ]
        ].combinations()
    }

    def 'should respect the message settings limits'() {
        given:
        def payload = new SplittablePayload(SplittablePayload.Type.INSERT, [new BsonDocument('a', new BsonBinary(new byte[900])),
                                                                            new BsonDocument('b', new BsonBinary(new byte[450])),
                                                                            new BsonDocument('c', new BsonBinary(new byte[450]))])
        def message = new CommandMessage(namespace, command, fieldNameValidator, ReadPreference.primary(), messageSettings,
                false, payload, fieldNameValidator)
        def output = new BasicOutputBuffer()
        def sessionContext = Stub(SessionContext)

        when:
        message.encode(output, sessionContext)
        def byteBuf = new ByteBufNIO(ByteBuffer.wrap(output.toByteArray()))
        def messageHeader = new MessageHeader(byteBuf, 2048)

        then:
        messageHeader.opCode == OpCode.OP_MSG.value
        messageHeader.requestId == RequestMessage.currentGlobalId - 1
        messageHeader.responseTo == 0
        byteBuf.getInt() == 0
        payload.getPosition() == pos1
        payload.hasAnotherSplit()

        when:
        payload = payload.getNextSplit()
        message = new CommandMessage(namespace, command, fieldNameValidator, ReadPreference.primary(), messageSettings,
                false, payload, fieldNameValidator)
        output.truncateToPosition(0)
        message.encode(output, sessionContext)
        byteBuf = new ByteBufNIO(ByteBuffer.wrap(output.toByteArray()))
        messageHeader = new MessageHeader(byteBuf, 1024)

        then:
        messageHeader.opCode == OpCode.OP_MSG.value
        messageHeader.requestId == RequestMessage.currentGlobalId - 1
        messageHeader.responseTo == 0
        byteBuf.getInt() == 1 << 1
        payload.getPosition() == pos2
        !payload.hasAnotherSplit()

        where:
        pos1 | pos2 | messageSettings
        1    | 2    | MessageSettings.builder().maxMessageSize(1024).serverVersion(new ServerVersion(3, 6)).build()
        2    | 1    | MessageSettings.builder().maxBatchCount(2).serverVersion(new ServerVersion(3, 6)).build()
    }

    def 'should throw if payload document bigger than max document size'() {
        given:
        def messageSettings = MessageSettings.builder().maxDocumentSize(900).serverVersion(new ServerVersion(3, 6)).build()
        def payload = new SplittablePayload(SplittablePayload.Type.INSERT, [new BsonDocument('a', new BsonBinary(new byte[900]))])
        def message = new CommandMessage(namespace, command, fieldNameValidator, ReadPreference.primary(), messageSettings,
                false, payload, fieldNameValidator)
        def output = new BasicOutputBuffer()
        def sessionContext = Stub(SessionContext)

        when:
        message.encode(output, sessionContext)

        then:
        thrown(BsonSerializationException)
    }

    private static BsonDocument getCommandDocument(ByteBufNIO byteBuf, MessageHeader messageHeader) {
        new ReplyMessage<BsonDocument>(new ResponseBuffers(new ReplyHeader(byteBuf, messageHeader), byteBuf),
                new BsonDocumentCodec(), 0).documents.get(0)
    }
}

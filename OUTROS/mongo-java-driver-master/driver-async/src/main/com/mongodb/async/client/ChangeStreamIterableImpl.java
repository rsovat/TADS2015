/*
 * Copyright 2017 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.async.client;

import com.mongodb.Block;
import com.mongodb.Function;
import com.mongodb.MongoNamespace;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.async.AsyncBatchCursor;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.operation.AsyncOperationExecutor;
import com.mongodb.operation.ChangeStreamOperation;
import org.bson.BsonDocument;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mongodb.assertions.Assertions.notNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

final class ChangeStreamIterableImpl<TResult> implements ChangeStreamIterable<TResult> {
    private final MongoNamespace namespace;
    private final ReadPreference readPreference;
    private final ReadConcern readConcern;
    private final CodecRegistry codecRegistry;
    private final AsyncOperationExecutor executor;
    private final List<? extends Bson> pipeline;
    private final Codec<ChangeStreamDocument<TResult>> codec;

    private Integer batchSize;
    private FullDocument fullDocument = FullDocument.DEFAULT;
    private BsonDocument resumeToken;
    private long maxAwaitTimeMS;
    private Collation collation;

    ChangeStreamIterableImpl(final MongoNamespace namespace, final CodecRegistry codecRegistry, final ReadPreference readPreference,
                             final ReadConcern readConcern, final AsyncOperationExecutor executor, final List<? extends Bson> pipeline,
                             final Class<TResult> resultClass) {
        this.namespace = notNull("namespace", namespace);
        this.codecRegistry = notNull("codecRegistry", codecRegistry);
        this.readPreference = notNull("readPreference", readPreference);
        this.readConcern = notNull("readConcern", readConcern);
        this.executor = notNull("executor", executor);
        this.pipeline = notNull("pipeline", pipeline);
        this.codec = ChangeStreamDocument.createCodec(notNull("resultClass", resultClass), codecRegistry);
    }

    @Override
    public ChangeStreamIterable<TResult> fullDocument(final FullDocument fullDocument) {
        this.fullDocument = notNull("fullDocument", fullDocument);
        return this;
    }

    @Override
    public ChangeStreamIterable<TResult> resumeAfter(final BsonDocument resumeToken) {
        this.resumeToken = notNull("resumeToken", resumeToken);
        return this;
    }

    @Override
    public ChangeStreamIterable<TResult> batchSize(final int batchSize) {
        this.batchSize = notNull("batchSize", batchSize);
        return this;
    }

    @Override
    public ChangeStreamIterable<TResult> maxAwaitTime(final long maxAwaitTime, final TimeUnit timeUnit) {
        notNull("timeUnit", timeUnit);
        this.maxAwaitTimeMS = MILLISECONDS.convert(maxAwaitTime, timeUnit);
        return this;
    }

    @Override
    public ChangeStreamIterable<TResult> collation(final Collation collation) {
        this.collation = notNull("collation", collation);
        return this;
    }

    @Override
    public <S> MongoIterable<S> withDocumentClass(final Class<S> resultClass) {
        return execute(codecRegistry.get(resultClass));
    }

    @Override
    public void first(final SingleResultCallback<ChangeStreamDocument<TResult>> callback) {
        notNull("callback", callback);
        execute(codec).first(callback);
    }

    @Override
    public void forEach(final Block<? super ChangeStreamDocument<TResult>> block, final SingleResultCallback<Void> callback) {
        notNull("block", block);
        notNull("callback", callback);
        execute(codec).forEach(block, callback);
    }

    @Override
    public <A extends Collection<? super ChangeStreamDocument<TResult>>> void into(final A target, final SingleResultCallback<A> callback) {
        notNull("target", target);
        notNull("callback", callback);
        execute(codec).into(target, callback);
    }

    @Override
    public <U> MongoIterable<U> map(final Function<ChangeStreamDocument<TResult>, U> mapper) {
        return new MappingIterable<ChangeStreamDocument<TResult>, U>(this, mapper);
    }

    @Override
    public void batchCursor(final SingleResultCallback<AsyncBatchCursor<ChangeStreamDocument<TResult>>> callback) {
        notNull("callback", callback);
        execute(codec).batchCursor(callback);
    }

    private <S> MongoIterable<S> execute(final Codec<S> codec) {
        List<BsonDocument> aggregateList = createBsonDocumentList(pipeline);

        ChangeStreamOperation<S> changeStreamOperation = new ChangeStreamOperation<S>(namespace, fullDocument, aggregateList, codec)
                .maxAwaitTime(maxAwaitTimeMS, MILLISECONDS)
                .batchSize(batchSize)
                .readConcern(readConcern)
                .collation(collation);

        if (resumeToken != null) {
            changeStreamOperation.resumeAfter(resumeToken);
        }

        return new OperationIterable<S>(changeStreamOperation, readPreference, executor);
    }

    private List<BsonDocument> createBsonDocumentList(final List<? extends Bson> pipeline) {
        List<BsonDocument> aggregateList = new ArrayList<BsonDocument>(pipeline.size());
        for (Bson obj : pipeline) {
            if (obj == null) {
                throw new IllegalArgumentException("pipeline can not contain a null value");
            }
            aggregateList.add(obj.toBsonDocument(BsonDocument.class, codecRegistry));
        }
        return aggregateList;
    }
}

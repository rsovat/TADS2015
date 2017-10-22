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

package com.mongodb;

import org.bson.BsonDocument;

/**
 * A MongoDB server session.
 *
 * @mongodb.server.release 3.6
 * @since 3.6
 */
public interface ServerSession {

    /**
     * @return the server session identifier
     */
    BsonDocument getIdentifier();

    /**
     * Return the next available transaction number.
     *
     * @return the next transaction number
     */
    long advanceTransactionNumber();

    /**
     * Whether the server session is closed.
     *
     * @return true if the session has been closed
     */
    boolean isClosed();
}

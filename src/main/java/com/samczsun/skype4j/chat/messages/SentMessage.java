/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.samczsun.skype4j.chat.messages;

import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.formatting.Message;

/**
 * Represents a message you sent
 */
public interface SentMessage extends ChatMessage {

    /**
     * Edit this message
     *
     * @param newMessage The message to edit it to
     * @throws ConnectionException If an exception occurs while connecting to the endpoint
     */
    void edit(Message newMessage) throws ConnectionException;

    /**
     * Delete this message
     *
     * @throws ConnectionException If an exception occurs while connecting to the endpoint
     */
    void delete() throws ConnectionException;
}

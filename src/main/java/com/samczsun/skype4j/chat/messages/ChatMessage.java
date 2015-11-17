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

import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.user.User;

/**
 * Represents a single message
 */
public interface ChatMessage {

    /**
     * Get the message object associated with this ChatMessage
     *
     * @return The message
     */
    Message getContent();

    /**
     * Get the time this message was sent or received at
     *
     * @return The time
     */
    long getSentTime();

    /**
     * Get the person who sent this message
     *
     * @return The user who sent this message
     */
    User getSender();

    /**
     * Get the chat this message was sent in. See {@link User#getChat()}
     *
     * @return The chat that the user belongs to
     */
    Chat getChat();

    /**
     * Get the {@link Skype} instance associated with this chat
     *
     * @return The Skype instance
     */
    Skype getClient();

    /**
     * Get the ID assigned to this message by the client. This is not guarenteed to be unique
     *
     * @return The ClientID
     */
    String getClientId();

    /**
     * Get the ID assigned to this message by Skype. Upon edits this ID should change but currently does not.
     * There appears to be no use for this ID.
     *
     * @return The ID
     */
    String getId();
}

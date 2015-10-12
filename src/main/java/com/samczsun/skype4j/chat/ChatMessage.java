/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
 *
 * This file is part of Skype4J.
 *
 * Skype4J is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Skype4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Skype4J.
 * If not, see http://www.gnu.org/licenses/.
 */

package com.samczsun.skype4j.chat;

import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.user.User;

/**
 * Represents a single message
 *
 * @author samczsun
 */
public interface ChatMessage {

    /**
     * Get the message object sent or received
     *
     * @return The message
     */
    Message getContent();

    /**
     * Get the time this message was sent or received at
     *
     * @return The time
     */
    long getTime();

    /**
     * Get the user that sent this message
     *
     * @return The user
     */
    User getSender();

    /**
     * Get the chat this message was sent in. See {@link User#getChat() User#getChat()}
     *
     * @return The chat that the user belongs to
     */
    Chat getChat();

    /**
     * Get the ID assigned to this message by the client. This is not guarenteed to be unique
     *
     * @return The ID
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

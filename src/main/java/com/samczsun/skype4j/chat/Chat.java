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

import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NotLoadedException;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.user.User;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chat. This can be a private message or a group chat.
 */
public interface Chat {
    /**
     * Sends a formatted message to this chat.
     *
     * @param message The rich text to send
     * @return The {@link ChatMessage ChatMessage} object representing the message
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     * @throws IOException If an unexpected error occurs
     * @throws NotLoadedException If the chat has not yet been loaded
     */
    ChatMessage sendMessage(Message message) throws ConnectionException, IOException;

    /**
     * Get the {@link User} object represented by that username. Usernames are case insensitive
     *
     * @param username The username of the user
     * @return The user object, or null if not found
     * @throws NotLoadedException If the chat has not yet been loaded
     */
    User getUser(String username);

    /**
     * Get the identity of the chat. Persistent across restarts
     *
     * @return The identity of this chat
     */
    String getIdentity();

    /**
     * Return a view of all the users in this chat
     *
     * @return All the users
     */
    Collection<User> getAllUsers();

    /**
     * Return a view of all the messages saved, in chronological order
     *
     * @return All the messages saved
     */
    List<ChatMessage> getAllMessages();

    /**
     * Get the {@link Skype} instance associated with this chat
     *
     * @return The Skype instance
     */
    Skype getClient();

    /**
     * Returns whether this chat has finished loading
     * Any calls to act upon the chat will throw a {@link NotLoadedException} if the chat is not loaded
     *
     * @return Whether the chat is loaded
     */
    boolean isLoaded();
}

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

package com.samczsun.skype4j.user;

import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NoPermissionException;

import java.util.List;

/**
 * Represents a user in a chat.
 * Multiple user instances may exist for a single contact
 */
public interface User {
    /**
     * Get the username of this user
     *
     * @return The username
     */
    String getUsername();

    /**
     * Get the displayname of this user.
     * This call will load contact data if it is not already loaded
     *
     * @return The displayname
     */
    String getDisplayName() throws ConnectionException;

    /**
     * Get the contact representation of this user
     * This call will load contact data if it is not already loaded
     *
     * @return The contact
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    Contact getContact() throws ConnectionException;

    /**
     * Get the role of this user
     *
     * @return The role
     */
    Role getRole();

    /**
     * Set the role of this user
     *
     * @param role The new role
     * @throws ConnectionException   If an error occurs while connecting to the endpoint
     * @throws NoPermissionException If a permission error occurs
     */
    void setRole(Role role) throws ConnectionException, NoPermissionException;

    /**
     * Get the chat this user is currently in
     *
     * @return The chat this user belongs to
     */
    Chat getChat();

    /**
     * Get the {@link Skype} instance associated with this user
     *
     * @return The Skype instance
     */
    Skype getClient();
    /**
     * Get all the messages sent by this user, in sequential order.
     * Messages sent when this API was not loaded will not be returned
     *
     * @return The sent messages
     */
    List<ChatMessage> getSentMessages();

    /**
     * Get the message based on the id
     *
     * @param id The id issued by Skype
     * @return The message
     */
    ChatMessage getMessageById(String id);

    enum Role {
        ADMIN, USER;

        public static Role getByName(String name) {
            return name.equalsIgnoreCase("admin") ? ADMIN : USER;
        }
    }
}

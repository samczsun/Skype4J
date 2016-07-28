/*
 * Copyright 2016 Sam Sun <me@samczsun.com>
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

package com.samczsun.skype4j.participants;

import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NoPermissionException;

import java.util.List;

/*
 * Represents a participant in a conversation.
 *
 * This participant could be an {@link User}, {@link Bot}, or any new participants that Skype decides to add
 */
public interface Participant {

    /*
     * Gets the {@link Skype} instance that this participant is associated with
     *
     * @returns The instance
     */
    Skype getClient();

    /*
     * Gets the id of this participant
     *
     * @returns The id
     */
    String getId();

    /*
     * Gets the display name of this participant. Implementation may vary.
     *
     * @returns The display name, or null if non existent or not found
     */
    String getDisplayName();

    /*
     * Gets the {@link Chat} this participant is participating in
     *
     * @returns The Chat object
     */
    Chat getChat();

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

    /**
     * Get the role of this user
     *
     * @return The role
     */
    Participant.Role getRole();

    /**
     * Set the role of this user
     *
     * @param role The new role
     * @throws ConnectionException   If an error occurs while connecting to the endpoint
     * @throws NoPermissionException If a permission error occurs
     */
    void setRole(Participant.Role role) throws ConnectionException, NoPermissionException;

    enum Role {
        ADMIN, USER;

        public static Role getByName(String name) {
            return name.equalsIgnoreCase("admin") ? ADMIN : USER;
        }
    }
}

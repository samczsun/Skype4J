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

package com.samczsun.skype4j.user;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NoSuchContactException;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;

/**
 * Represents a contact
 */
public interface Contact {
    /**
     * Get the username of this contact
     *
     * @return The username
     */
    String getUsername();

    /**
     * Get the displayname of this contact. Can return null if not found
     *
     * @return The displayname
     */
    String getDisplayName();

    /**
     * Get the first name of this contact. Can return null if not found
     *
     * @return The first name
     */
    String getFirstName();

    /**
     * Get the last name of this contact. Can return null if not found
     *
     * @return The last name
     */
    String getLastName();

    /**
     * Get the avatar as a {@link BufferedImage}. Can return null if not found.
     * Once the image is loaded it is cached
     * so if the first call suceeds it is implied no more exceptions will be thrown
     *
     * @return A clone of the original BufferedImage
     * @throws ConnectionException If an error occurs while fetching the original image
     */
    BufferedImage getAvatarPicture() throws ConnectionException;

    /**
     * Get the avatar as a URL. Can return null if not found.
     *
     * @return The URL to the avatar
     */
    String getAvatarURL();

    /**
     * Get the mood of this contact. Can return null if not found
     *
     * @return The mood
     */
    String getMood();

    /**
     * Get the mood as richtext of this contact. Can return null if not found
     *
     * @return The mood as richtext
     */
    @Deprecated
    String getRichMood();

    /**
     * Get the country this contact lives in. Can return null if not found
     *
     * @return The country
     */
    String getCountry();

    /**
     * Get the city this contact lives in. Can return null if not found
     *
     * @return The city
     */
    String getCity();

    /**
     * Get whether this contact is authorized by you to be a contact
     *
     * @return Whether the contact is authorized
     */
    boolean isAuthorized();

    /**
     * Authorize this contact
     *
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void authorize() throws ConnectionException;

    /**
     * Unauthorize this contact
     *
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void unauthorize() throws ConnectionException;

    /**
     * Send a authorization request to this user
     * @param message The message to attach
     * @throws ConnectionException If an error occurs while sending the request
     * @throws NoSuchContactException If the user does not exist
     */
    void sendRequest(String message) throws ConnectionException, NoSuchContactException;

    /**
     * Get whether this contact is blocked
     *
     * @return Whether the contact is blocked
     */
    boolean isBlocked();

    /**
     * Block this contact
     *
     * @param reportAbuse Whether to report abuse
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void block(boolean reportAbuse) throws ConnectionException;

    /**
     * Unblock this contact
     *
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void unblock() throws ConnectionException;

    /**
     * Get whether this contact is a phone number
     *
     * @return Whether this contact is a phone number
     */
    boolean isPhone();

    /**
     * Get the private conversation between you and this user
     * @return The private conversation
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     * @throws ChatNotFoundException If the contact does not exist or is a phone
     */
    Chat getPrivateConversation() throws ConnectionException, ChatNotFoundException;
}

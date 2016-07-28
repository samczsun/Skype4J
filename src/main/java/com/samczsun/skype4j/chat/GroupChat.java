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

package com.samczsun.skype4j.chat;

import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.participants.info.Contact;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Represents a group chat with one or more people
 */
public interface GroupChat extends Chat {
    /**
     * Get the topic of the chat.
     *
     * @return The topic of this chat
     */
    String getTopic();

    /**
     * Set the topic of the chat. This will occur in real time
     *
     * @param topic The topic
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void setTopic(String topic) throws ConnectionException;

    /**
     * Get the current picture of the chat.
     * The result will be cached unless the picture is updated
     *
     * @return The current picture
     * @throws ConnectionException If an error occurs while fetching the picture.
     */
    BufferedImage getPicture() throws ConnectionException;

    /**
     * Set the image for this chat. This will occur in real time
     * WARNING: This endpoint is not officially supported by Skype.
     * As such, it may or may not be functional in the future
     *
     * @param image     The image to upload
     * @param imageType The type of image (png, jpg, etc)
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     * @throws IOException If an error occurs while converting the image to bytes
     */
    void setImage(BufferedImage image, String imageType) throws ConnectionException, IOException;

    /**
     * Set the image for this chat. This will occur in real time
     * WARNING: This endpoint is not officially supported by Skype.
     * As such, it may or may not be functional in the future
     *
     * @param file     The image to upload
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     * @throws IOException If an error occurs while converting the image to bytes
     */
    void setImage(File file) throws ConnectionException, IOException;

    /**
     * Get whether an option is enabled.
     *
     * @param option The option to query
     * @return Whether the option is enabled
     */
    boolean isOptionEnabled(Option option);

    /**
     * Set whether an option is enabled.
     * If the option is already enabled and a request to enable it is sent or vice versa, it will be silently ignored
     *
     * @param option  The option to set
     * @param enabled Whether to enable it or not
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void setOptionEnabled(Option option, boolean enabled) throws ConnectionException;

    /**
     * Add a contact into this chat. This will occur in real time
     *
     * @param contact The contact to add
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void add(Contact contact) throws ConnectionException;

    /**
     * Kick a user from this chat. This will occur in real time.
     *
     * @param username The username of the user to kick
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void kick(String username) throws ConnectionException;

    /*
     * Bans an id from this group chat.
     *
     * @param id The id to ban
     */
    void ban(String id) throws ConnectionException;

    /*
     * Unbans an id from this group chat.
     *
     * @param id The id to unban
     */
    void unban(String id) throws ConnectionException;

    /*
     * Gets the currently banned ids. Note that this may be out of sync if you happen to lose internet connection
     *
     * @returns A view of the currently banned ids, as recorded locally
     */
    List<String> getBannedIds();

    /*
     * Whitelists the id. The whitelist will only be active if there is one or more ids whitelisted.
     *
     * Non-whitelisted ids will be kicked as if they were banned
     *
     * @param id The id to whitelist
     */
    void whitelist(String id) throws ConnectionException;

    /*
     * Unwhitelists the id. If there are zero ids whitelisted the whitelist will be turned off by Skype, meaning anyone can join or be added.
     *
     * @param id The id to unwhitelist
     */
    void unwhitelist(String id) throws ConnectionException;

    /*
     * Gets the currently whitelisted ids. Note that this may be out of sync if you happen to lose internet connection
     *
     * @returns A view of the currently whitelisted ids, as recorded locally
     */
    List<String> getWhitelistedIds();

    /**
     * Leave the chat. This will occur in real time.
     *
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void leave() throws ConnectionException;

    /**
     * Gets the join url for people to join.
     *
     * @return The join url
     * @throws ConnectionException   If an error occurs while connecting to the endpoint
     * @throws IllegalStateException If joining is not enabled
     */
    String getJoinUrl() throws ConnectionException;

    enum Option {
        JOINING_ENABLED("joiningenabled"),
        HISTORY_DISCLOSED("historydisclosed"),
        LOCK_TOPIC_AND_PICTURE("moderatedthread");

        private String id;

        Option(String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }
    }
}

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

import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.handler.ErrorHandler;
import com.samczsun.skype4j.formatting.IMoji;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.participants.Participant;
import com.samczsun.skype4j.participants.info.Contact;
import com.samczsun.skype4j.participants.User;

import java.awt.image.BufferedImage;
import java.io.File;
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
     * @return The {@link ChatMessage} object representing the message
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    ChatMessage sendMessage(Message message) throws ConnectionException;

    /**
     * Sends a plain message to this chat.
     *
     * @param plainMessage The plain message to send
     * @return The {@link ChatMessage} object representing the message
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    ChatMessage sendMessage(String plainMessage) throws ConnectionException;

    /**
     * Sends a contact to this chat
     *
     * @param contact The contact to send
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void sendContact(Contact contact) throws ConnectionException;

    /**
     * Sends an image to this chat
     *
     * @param image     The image to send
     * @param imageType The type of image (jpg, png, etc)
     * @param imageName The name of the image
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     * @throws IOException If an error occurs while reading the image file
     */
    void sendImage(BufferedImage image, String imageType, String imageName) throws ConnectionException, IOException;

    /**
     * Sends an image to this chat
     *
     * @param image The file containing the image
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     * @throws IOException If an error occurs while reading the image file
     */
    void sendImage(File image) throws ConnectionException, IOException;

    /**
     * Sends a file to this chat
     *
     * @param file The file to send
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void sendFile(File file) throws ConnectionException;

    /**
     * Sends a Moji to this chat
     *
     * @param moji The appropriate lang-based Flik message
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void sendMoji(IMoji moji) throws ConnectionException;

    /**
     * Get a participant based on their id. The ID is in the format of {number}:{string}
     *
     * For example, a user has id 8:{username} while a bot has id 28:{uuid}
     *
     * @param id The id of the participant
     * @return The participant, or null if not found
     */
    Participant getParticipant(String id);

    /**
     * Get yourself!
     *
     * @return Your user object!
     */
    User getSelf();

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
    Collection<Participant> getAllParticipants();

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
     * Sets your alerts off. Does not affect anything in this API
     *
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void alertsOff() throws ConnectionException;


    /**
     * Sets your alerts on. Does not affect anything in this API
     *
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void alertsOn() throws ConnectionException;

    /**
     * Sets your alerts on to a keyword. Does not affect anything in this API
     *
     * @param keyword The word to alert to
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void alertsOn(String keyword) throws ConnectionException;

    /**
     * Load more chatmessages from the past
     *
     * @param amount The amount of messages to load
     * @return The loaded messages
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    List<ChatMessage> loadMoreMessages(int amount) throws ConnectionException;

    /*
     * Send the typing notification. Note that this will spawn a new thread so supplying an ErrorHandler is recommended
     * If typing is already being sent no action will be taken.
     *
     * If an error occurs the default handler will be notified
     */
    default void startTyping() {
        startTyping(null);
    }

    /*
     * Send the typing notification. If an connection exception occurs the specified handler as well
     * as the default handler will be notified
     */
    void startTyping(ErrorHandler handler);

    /*
     * Stop sending the typing notification
     */
    void stopTyping();

    /*
     * Forces a synchronization of this Chat with the remote Chat.
     *
     * What this updates is up to the implementation
     */
    void sync() throws ConnectionException;
}

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

package com.samczsun.skype4j;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.events.EventDispatcher;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.InvalidCredentialsException;
import com.samczsun.skype4j.exceptions.NoPermissionException;
import com.samczsun.skype4j.exceptions.NotParticipatingException;
import com.samczsun.skype4j.exceptions.ParseException;
import com.samczsun.skype4j.exceptions.handler.ErrorSource;
import com.samczsun.skype4j.user.Contact;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class represents a single Skype account, which may or may not have been logged in
 */
public interface Skype {

    /**
     * Log into Skype. This will perform the following actions:
     * 1) Log into Skype to get a SkypeToken
     * 2) Register an endpoint to get a RegistrationToken
     *
     * Note that the SkypeToken technically expires after 24 hours. The vanilla implementation in Skype for Web
     * is to redirect you to the login screen. As such, roughly half an hour before 24 hours is hit, the API
     * will attempt to re-login and, if subscribed, resubscribe.
     *
     * Note that on the off chance that a CAPTCHA is encountered, you will need to deal with it accordingly
     * by listening to {@link com.samczsun.skype4j.events.misc.CaptchaEvent CaptchaEvent}.
     *
     * @throws InvalidCredentialsException If you've provided invalid credentials or if you hit a CAPTCHA
     * @throws ConnectionException         If a network error occured while connecting
     * @throws ParseException              If invalid HTML/XML was returned, causing Jsoup to raise an exception
     * @throws NotParticipatingException   If the guest account cannot log in due to the chat not being open
     */
    void login() throws InvalidCredentialsException, ConnectionException, ParseException, NotParticipatingException;

    /**
     * Subscribe to the HTTP long polling service.
     * This will start reading events from Skype and calling events within this API.
     * Please note this call is not necessary if you do not plan on using the event API.
     *
     * @throws ConnectionException If an connection error occurs during subscription
     */
    void subscribe() throws ConnectionException;

    /**
     * Get the username of the account logged in
     *
     * @return The username
     */
    String getUsername();

    /**
     * Get a {@link Chat} based on the identity given. The chat must already be loaded
     * The identity is a Skype-assigned id that begins with {@code 19:} or {@code 8:}
     *
     * @param name The identity of the chat
     * @return The {@link Chat}, or null if not found
     */
    Chat getChat(String name);

    /**
     * Load a {@link Chat} given an identity
     * The identity is a Skype-assigned id that begins with {@code 19:} or {@code 8:}
     *
     * @param name The identity of the chat
     * @return The newly loaded {@link Chat}
     * @throws ConnectionException   If an error occurs during connection
     * @throws ChatNotFoundException If this skype account is not a member of the chat
     */
    Chat loadChat(String name) throws ConnectionException, ChatNotFoundException;

    /**
     * Get a chat, and if said chat doesn't exist, load it
     *
     * @param name The name of the chat
     * @return The chat
     * @throws ConnectionException   If an exception occurs while fetching chat details
     * @throws ChatNotFoundException If the chat does not exist
     */
    Chat getOrLoadChat(String name) throws ConnectionException, ChatNotFoundException;

    /**
     * Join the chat with the given id in the format of 19:xxxxx@thread.skype
     *
     * @param chatId The skype chat id
     * @return The group chat object
     * @throws ConnectionException   If an exception occurs while joining the chat
     * @throws ChatNotFoundException If the chat does not exist
     * @throws NoPermissionException If the chat is not public
     */
    GroupChat joinChat(String chatId) throws ConnectionException, ChatNotFoundException, NoPermissionException;

    /**
     * Load amount of chats in the past
     *
     * @param amount The amount of chats
     * @return The chats loaded
     * @throws ConnectionException If an error occurs while connecting
     */
    List<Chat> loadMoreChats(int amount) throws ConnectionException;

    /**
     * Get a contact based on the username. The contact must already be loaded
     *
     * @param username The username of the contact
     * @return The {@link Contact Contact} object, or null if not found
     */
    Contact getContact(String username);

    /**
     * Load a contact given a username
     *
     * @param username The username of the contact
     * @return The contact that was loaded
     * @throws ConnectionException If an exception occured while fetching contact details
     */
    Contact loadContact(String username) throws ConnectionException;

    /**
     * Get a contact, and if said contact doesn't exist, load it
     *
     * @param username The username of the contact
     * @return The contact
     * @throws ConnectionException If an exception occured while fetching contact details
     */
    Contact getOrLoadContact(String username) throws ConnectionException;

    /**
     * Load all contacts!
     *
     * @throws ConnectionException If an exception occured while fetching all contacts
     */
    void loadAllContacts() throws ConnectionException;

    /**
     * Get all the chats loaded by this API
     *
     * @return A view of all the chats
     */
    Collection<Chat> getAllChats();

    /**
     * Get all the contacts loaded by this API
     *
     * @return A view of all the chats
     */
    Collection<Contact> getAllContacts();

    /**
     * Log out and stop all threads
     *
     * @throws ConnectionException If an error occurs while logging out
     */
    void logout() throws ConnectionException;

    /**
     * Get the event dispatcher that handles listening to events
     *
     * @return The {@link EventDispatcher EventDispatcher}
     */
    EventDispatcher getEventDispatcher();

    /**
     * Get the Logger used for debugging
     *
     * @return The Logger
     */
    Logger getLogger();

    /**
     * Create a new group chat with the selected contacts. You will be automatically added to the group
     *
     * @param contacts The contacts to add
     * @return The newly created group chat
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    GroupChat createGroupChat(Contact... contacts) throws ConnectionException;

    /**
     * Set your current visibility
     *
     * @param visibility The visibility to set
     * @throws ConnectionException If an error occurs while connecting to the endpoint
     */
    void setVisibility(Visibility visibility) throws ConnectionException;

    void handleError(ErrorSource errorSource, Throwable throwable, boolean shutdown);
}

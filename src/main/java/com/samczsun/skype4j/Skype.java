package com.samczsun.skype4j;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.events.EventDispatcher;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.InvalidCredentialsException;
import com.samczsun.skype4j.exceptions.ParseException;
import com.samczsun.skype4j.user.Contact;

import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.logging.Logger;

public abstract class Skype {

    /**
     * Subscribe to the HTTP long polling service. This will start reading
     * events from Skype and calling events within this API
     *
     * @throws IOException Thrown if any internal operations go wrong
     */
    public abstract void subscribe() throws IOException;

    /**
     * Get the username of the account logged in
     *
     * @return The username
     */
    public abstract String getUsername();

    /**
     * Get a chat based on the identity given. If no chat is found, null will be returned
     *
     * @param name The identity of the chat
     * @return The {@link Chat Chat} object, or null if not found
     */
    public abstract Chat getChat(String name);

    /**
     * Load a chat given an identity
     */
    public abstract Chat loadChat(String name) throws ConnectionException, ChatNotFoundException;

    /**
     * Get a contact based on the username. If no contact is found, null will be returned
     *
     * @param username The username of the contact
     * @return The {@link Contact Contact} object, or null if not found
     */
    public abstract Contact getContact(String username);

    /**
     * Load a contact given a username
     *
     * @param username The username of the contact
     * @return The contact that was loaded
     * @throws ConnectionException If an exception occured while fetching contact details
     */
    public abstract Contact loadContact(String username) throws ConnectionException;

    /**
     * Get a contact, and if said contact doesn't exist, load it
     * @param username The username of the contact
     * @return The contact
     * @throws ConnectionException If an exception occured while fetching contact details
     */
    public abstract Contact getOrLoadContact(String username) throws ConnectionException;

    /**
     * Get all the chats loaded by this API
     *
     * @return A view of all the chats
     */
    public abstract Collection<Chat> getAllChats();

    /**
     * Get all the contacts loaded by this API
     *
     * @return A view of all the chats
     */
    public abstract Collection<Contact> getAllContacts();

    /**
     * Log into Skype
     *
     * @throws InvalidCredentialsException If you've provided invalid credentials or if you hit a CAPTCHA
     * @throws ConnectionException         If a network error occured while connecting
     * @throws ParseException              If invalid HTML/XML was returned, causing Jsoup to raise an exception
     */
    public abstract void login() throws InvalidCredentialsException, ConnectionException, ParseException;

    /**
     * Log out and stop all threads
     *
     * @throws IOException
     */
    public abstract void logout() throws ConnectionException;

    /**
     * Get the event dispatcher that handles listening to events
     *
     * @return The {@link EventDispatcher EventDispatcher}
     */
    public abstract EventDispatcher getEventDispatcher();

    /**
     * Get the Logger used for debugging
     *
     * @return The Logger
     */
    public abstract Logger getLogger();

    /**
     * Create a new group chat with the selected contacts. You will be automatically added to the group
     * If an error occurs while creating the chat, an {@link ConnectionException} or an {@link ChatNotFoundException} will be thrown
     *
     * @param contacts The contacts to add
     * @return The newly created group chat
     * @throws ConnectionException
     * @throws ChatNotFoundException
     */
    public abstract GroupChat createGroupChat(Contact... contacts) throws ConnectionException, ChatNotFoundException;
}

package com.samczsun.skype4j;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.EventDispatcher;

import java.io.IOException;
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
     * Get a chat based on the identity given. If no chat is found, it will be
     * loaded if it exists
     *
     * @param name The identity of the chat
     * @return The {@link Chat Chat} object, or null if not found
     */
    public abstract Chat getChat(String name);

    /**
     * Get all the chats loaded by this API
     *
     * @return A view of all the chats
     */
    public abstract Collection<Chat> getAllChats();

    /**
     * Log out and stop all threads
     *
     * @throws IOException
     */
    public abstract void logout() throws IOException;

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
}

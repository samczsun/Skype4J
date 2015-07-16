package com.samczsun.skype4j.chat;

import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.user.User;

import java.util.Collection;
import java.util.List;

/**
 * Represents a single chat. This can be a private message or a group chat.
 *
 * @author samczsun
 */
public interface Chat {
    /**
     * Sends a formatted message to this chat
     *
     * @param message The rich text to send
     * @return The {@link ChatMessage ChatMessage} object representing the
     * message
     * @throws SkypeException
     */
    ChatMessage sendMessage(Message message) throws SkypeException;

    /**
     * Get the {@link User User} object represented by that username
     *
     * @param username The username of the user
     * @return The user object
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
     * Returns whether this chat has finished loading. Any calls to act upon the
     * chat will throw a {@link com.samczsun.skype4j.exceptions.NotLoadedException NotLoadedException} if the chat is not loaded
     *
     * @return The loaded state
     */
    boolean isLoaded();
}

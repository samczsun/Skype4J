package com.samczsun.skype4j.chat;

import java.util.Collection;
import java.util.List;

import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Text;
import com.samczsun.skype4j.user.User;

/**
 * Represents a single chat. This can be a private message or a group chat.
 * 
 * @author samczsun
 */
public interface Chat {

    /**
     * Fetches the list of members in the group, along with roles, and updates
     * the internal database. If {@link #getType() getType()} returns a type of
     * {@link Type#GROUP GROUP}, an HTTP request will be made
     * 
     * @throws SkypeException
     */
    public void updateUsers() throws SkypeException;

    /**
     * Sends a formatted message to this chat
     * 
     * @param message
     *            The rich text to send
     * @return The {@link ChatMessage ChatMessage} object representing the
     *         message
     * @throws SkypeException
     */
    public ChatMessage sendMessage(Text message) throws SkypeException;

    /**
     * Get the {@link User User} object represented by that username
     * 
     * @param username
     *            The username of the user
     * @return The user object
     */
    public User getUser(String username);

    /**
     * Get the {@link ChatMessage ChatMessage} object associated with this
     * Skype-assigned id
     * 
     * @param id
     *            The skype id, not client id
     * @return The ChatMessage object
     */
    public ChatMessage getMessage(String id);

    /**
     * Get the identity of the chat, or the output of /showname in chat
     * 
     * If the return of {@link #getType() getType()} is {@link Type#GROUP GROUP}
     * , the result will start with "19:" Otherwise, the result will start with
     * "8:"
     * 
     * @return The identity of this chat
     */
    public String getIdentity();

    /**
     * Get the topic of the chat.
     * 
     * @return The topic
     */
    public String getTopic();

    /**
     * Set the topic of the chat. This will update it in real time
     * 
     * @param topic
     *            The topic
     * @throws SkypeException
     */
    public void setTopic(String topic) throws SkypeException;

    /**
     * Return a view of all the users in this chat
     * 
     * @return All the users
     */
    public Collection<User> getAllUsers();

    /**
     * Return a view of all the messages saved, in chronological order
     * 
     * @return All the messages saved
     */
    public List<ChatMessage> getAllMessages();

    /**
     * Get the type of chat this is
     * 
     * @return A enum value of {@link Type Type}
     */
    public Type getType();

    /**
     * An Enum to represent the different types of chats
     * 
     * @author samczsun
     *
     */
    public static enum Type {
        /**
         * Represents a private chat with one other person
         */
        INDIVIDUAL,
        /**
         * Represents a group chat with one or more people
         */
        GROUP;
    }
}

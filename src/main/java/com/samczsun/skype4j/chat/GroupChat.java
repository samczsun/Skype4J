package com.samczsun.skype4j.chat;

import com.samczsun.skype4j.exceptions.SkypeException;

/**
 * Represents a group chat with one or more people
 * 
 * @author Sam
 *
 */
public interface GroupChat extends Chat {
    /**
     * Get the topic of the chat.
     * 
     * @return The topic
     */
    String getTopic();

    /**
     * Set the topic of the chat. This will update it in real time
     * 
     * @param topic
     *            The topic
     * @throws SkypeException
     */
    void setTopic(String topic) throws SkypeException;

    /**
     * Kick a user from this chat. Is not supported in individual chats.
     * 
     * @param username The username of the user to kick
     * @throws SkypeException
     *             If the user is not in this chat, or if the kick failed
     */
    void kick(String username) throws SkypeException;
}

package com.samczsun.skype4j.chat;

import com.samczsun.skype4j.events.chat.user.action.OptionUpdateEvent;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NotLoadedException;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.user.Contact;

/**
 * Represents a group chat with one or more people
 *
 * @author samczsun
 */
public interface GroupChat extends Chat {
    /**
     * Get the topic of the chat.
     * If the chat is not loaded a {@link com.samczsun.skype4j.exceptions.NotLoadedException NotLoadedException} will be thrown
     *
     * @return The topic of this chat
     * @throws NotLoadedException
     */
    String getTopic();

    /**
     * Set the topic of the chat. This will occur in real time.
     * If an exception occurs while connecting updating the topic, a {@link com.samczsun.skype4j.exceptions.ConnectionException ConnectionException} will be thrown
     * If the chat is not loaded a {@link com.samczsun.skype4j.exceptions.NotLoadedException NotLoadedException} will be thrown
     *
     * @param topic The topic
     * @throws ConnectionException
     * @throws NotLoadedException
     */
    void setTopic(String topic) throws ConnectionException;

    /**
     * Get whether an option is enabled.
     * If the chat is not loaded a {@link com.samczsun.skype4j.exceptions.NotLoadedException NotLoadedException} will be thrown
     *
     * @param option The option to query
     * @return Whether the option is enabled
     * @throws NotLoadedException
     */
    boolean isOptionEnabled(OptionUpdateEvent.Option option);

    /**
     * Set whether an option is enabled.
     * If the option is already enabled and a request to enable it is sent or vice versa, it will be silently ignored
     * If an exception occurs while setting the option, a {@link com.samczsun.skype4j.exceptions.ConnectionException ConnectionException} will be thrown
     * If the chat is not loaded a {@link com.samczsun.skype4j.exceptions.NotLoadedException NotLoadedException} will be thrown
     *
     * @param option The option to set
     * @param enabled Whether to enable it or not
     * @throws ConnectionException
     * @throws NotLoadedException
     */
    void setOptionEnabled(OptionUpdateEvent.Option option, boolean enabled) throws ConnectionException;

    /**
     * Add a contact into this chat. This will occur in real time
     * If an exception occurs while adding the user, a {@link com.samczsun.skype4j.exceptions.ConnectionException ConnectionException} will be thrown
     * If the chat is not loaded a {@link com.samczsun.skype4j.exceptions.NotLoadedException NotLoadedException} will be thrown
     *
     * @param contact The contact to add
     * @throws ConnectionException
     * @throws NotLoadedException
     */
    void add(Contact contact) throws ConnectionException;

    /**
     * Kick a user from this chat. This will occur in real time.
     * If an exception occurs while kicking the user, a {@link com.samczsun.skype4j.exceptions.ConnectionException ConnectionException} will be thrown
     * If the chat is not loaded a {@link com.samczsun.skype4j.exceptions.NotLoadedException NotLoadedException} will be thrown
     *
     * @param username The username of the user to kick
     * @throws ConnectionException
     * @throws NotLoadedException
     */
    void kick(String username) throws ConnectionException;

    /**
     * Leave the chat. This will occur in real time.
     * If an exception occurs while leaving the chat, a {@link com.samczsun.skype4j.exceptions.ConnectionException ConnectionException} will be thrown
     * If the chat is not loaded a {@link com.samczsun.skype4j.exceptions.NotLoadedException NotLoadedException} will be thrown
     *
     * @throws ConnectionException
     * @throws NotLoadedException
     */
    void leave() throws ConnectionException;

    /**
     * Gets the join url for people to join.
     * If joining is not enabled, an {@link IllegalArgumentException} will be thrown
     * If an exception occurs while getting the link, a {@link com.samczsun.skype4j.exceptions.ConnectionException ConnectionException} will be thrown
     * If the chat is not loaded a {@link com.samczsun.skype4j.exceptions.NotLoadedException NotLoadedException} will be thrown
     *
     * @return The join url
     * @throws ConnectionException
     * @throws NotLoadedException
     * @throws IllegalArgumentException
     */
    String getJoinUrl() throws ConnectionException;
}

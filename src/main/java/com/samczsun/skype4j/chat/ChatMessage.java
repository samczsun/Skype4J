package com.samczsun.skype4j.chat;

import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.user.User;

/**
 * Represents a single message
 *
 * @author samczsun
 */
public interface ChatMessage {

    /**
     * Get the message object sent or received
     *
     * @return The message
     */
    Message getContent();

    /**
     * Get the time this message was sent or received at
     *
     * @return The time
     */
    long getTime();

    /**
     * Get the user that sent this message
     *
     * @return The user
     */
    User getSender();

    /**
     * Get the chat this message was sent in. See {@link User#getChat() User#getChat()}
     *
     * @return The chat that the user belongs to
     */
    Chat getChat();

    /**
     * Get the ID assigned to this message by the client. This is not guarenteed to be unique
     *
     * @return The ID
     */
    String getClientId();

    /**
     * Get the ID assigned to this message by Skype. Upon edits this ID should change but currently does not.
     * There appears to be no use for this ID.
     *
     * @return The ID
     */
    String getId();
}

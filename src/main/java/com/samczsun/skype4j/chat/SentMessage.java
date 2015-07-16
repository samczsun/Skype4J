package com.samczsun.skype4j.chat;

import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Message;

/**
 * Represents a message you sent
 *
 * @author samczsun
 */
public interface SentMessage extends ChatMessage {

    /**
     * Edit this message
     *
     * @param newMessage The message to edit it to
     * @throws SkypeException If something goes wrong during the editing
     */
    void edit(Message newMessage) throws SkypeException;

    /**
     * Delete this message
     *
     * @throws SkypeException If something goes wrong while deleting
     */
    void delete() throws SkypeException;
}

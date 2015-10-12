/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
 *
 * This file is part of Skype4J.
 *
 * Skype4J is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Skype4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Skype4J.
 * If not, see http://www.gnu.org/licenses/.
 */

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

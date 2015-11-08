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

package com.samczsun.skype4j.chat.objects;

/**
 * Represents a file which has been sent by a user in the chat
 * Note that downloading files is not supported on Skype for Web, even if the chat is cloud-based
 */
public interface ReceivedFile {

    /**
     * Get the name of the file which has been sent
     * @return The name of the file
     */
    String getName();

    /**
     * Get the size of the file
     * @return The filesize
     */
    long getSize();

    /**
     * Get the tid. Not sure what this does
     * @return
     */
    long getTid();
}

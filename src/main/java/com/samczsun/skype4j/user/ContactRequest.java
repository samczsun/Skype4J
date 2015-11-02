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

package com.samczsun.skype4j.user;

import com.samczsun.skype4j.exceptions.ConnectionException;

import java.util.Date;

/**
 * Represents a contact request that is pending.
 *
 * TODO: Move to a different package?
 */
public interface ContactRequest {

    /**
     * Get the time at which this contact request was sent
     * @return The time it was sent
     */
    Date getTime();

    /**
     * Get the user who sent this contact request
     * @return The contact
     */
    Contact getSender();

    /**
     * Get the custom message sent by the sender
     * @return The message
     */
    String getMessage();

    /**
     * Accept the contact request represented by this object
     * @throws ConnectionException If the accepting of the contact request failed
     */
    void accept() throws ConnectionException;
}

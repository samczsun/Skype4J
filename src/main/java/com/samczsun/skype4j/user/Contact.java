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

/**
 * Represents a contact - basically an object who will represent a specific user in any chat
 */
public interface Contact {
    /**
     * Get the username of this contact
     * @return the username
     */
    public String getUsername();

    /**
     * Get the displayname of this contact. Can return null if it cannot be found
     * @return the displayname
     */
    public String getDisplayName();
}

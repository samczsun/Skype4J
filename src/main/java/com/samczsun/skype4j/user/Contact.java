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

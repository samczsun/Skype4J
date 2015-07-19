package com.samczsun.skype4j.chat;

import com.samczsun.skype4j.user.User;

/**
 * Represents a private conversation between the user logged in and one other
 * user
 *
 * @author samczsun
 */
public interface IndividualChat extends Chat {
    /**
     * Gets the conversation partner.
     *
     * @return A User object representing the conversation partner
     */
    User getPartner();
}

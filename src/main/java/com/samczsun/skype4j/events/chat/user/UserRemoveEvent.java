package com.samczsun.skype4j.events.chat.user;

import com.samczsun.skype4j.user.User;

public class UserRemoveEvent extends UserEvent {
    private final User initiator;

    public UserRemoveEvent(User user, User initiator) {
        super(user);
        this.initiator = initiator;
    }

    public User getInitiator() {
        return this.initiator;
    }
}

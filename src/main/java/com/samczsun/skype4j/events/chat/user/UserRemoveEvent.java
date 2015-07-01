package com.samczsun.skype4j.events.chat.user;

import com.samczsun.skype4j.chat.User;

public class UserRemoveEvent extends UserEvent {
    public UserRemoveEvent(User user) {
        super(user);
    }
}

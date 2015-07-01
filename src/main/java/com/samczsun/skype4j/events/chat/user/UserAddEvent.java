package com.samczsun.skype4j.events.chat.user;

import com.samczsun.skype4j.chat.User;

public class UserAddEvent extends UserEvent {
    public UserAddEvent(User user) {
        super(user);
    }
}

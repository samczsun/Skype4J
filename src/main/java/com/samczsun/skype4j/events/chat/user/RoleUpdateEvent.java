package com.samczsun.skype4j.events.chat.user;

import com.samczsun.skype4j.user.User;

public class RoleUpdateEvent extends UserEvent {
    public RoleUpdateEvent(User user) {
        super(user);
    }
}

package com.samczsun.skype4j.events.chat.user;

import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.user.User;

public abstract class UserEvent extends ChatEvent {
    private final User user;
    
    public UserEvent(User user) {
        super(user.getChat());
        this.user = user;
    }
    
    public User getUser() {
        return this.user;
    }
}

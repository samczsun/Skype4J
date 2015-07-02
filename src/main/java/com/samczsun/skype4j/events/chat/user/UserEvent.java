package com.samczsun.skype4j.events.chat.user;

import com.samczsun.skype4j.chat.User;
import com.samczsun.skype4j.events.chat.ChatEvent;

public abstract class UserEvent extends ChatEvent {
    private User user;
    
    public UserEvent(User user) {
        super(user.getChat());
        this.user = user;
    }
    
    public User getUser() {
        return this.user;
    }
}

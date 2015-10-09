package com.samczsun.skype4j.events.chat.user.action;

import com.samczsun.skype4j.events.chat.user.UserEvent;
import com.samczsun.skype4j.user.User;

public class RoleUpdateEvent extends UserEvent {
    private long time;
    private User target;
    private User.Role newRole;

    public RoleUpdateEvent(User initiator, long time, User target, User.Role newRole) {
        super(initiator);
        this.time = time;
        this.target = target;
        this.newRole = newRole;
    }

    public long getEventTime() {
        return this.time;
    }

    public User getTarget() {
        return this.target;
    }

    public User.Role getNewRole() {
        return this.newRole;
    }
}

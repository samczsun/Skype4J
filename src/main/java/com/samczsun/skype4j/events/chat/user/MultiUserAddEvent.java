package com.samczsun.skype4j.events.chat.user;

import java.util.Collections;
import java.util.List;

import com.samczsun.skype4j.user.User;

public class MultiUserAddEvent extends UserAddEvent {
    private final List<User> allUsers;

    public MultiUserAddEvent(List<User> users, User initiator) {
        super(users.get(0), initiator);
        allUsers = users;
    }
    
    public List<User> getAllUsers() {
        return Collections.unmodifiableList(allUsers);
    }
}

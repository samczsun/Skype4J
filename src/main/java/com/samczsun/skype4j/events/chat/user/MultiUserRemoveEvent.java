package com.samczsun.skype4j.events.chat.user;

import java.util.Collections;
import java.util.List;

import com.samczsun.skype4j.chat.User;

public class MultiUserRemoveEvent extends UserRemoveEvent {
    private List<User> allUsers;

    public MultiUserRemoveEvent(List<User> users, User initiator) {
        super(users.get(0), initiator);
        allUsers = users;
    }
    
    public List<User> getAllUsers() {
        return Collections.unmodifiableList(allUsers);
    }
}

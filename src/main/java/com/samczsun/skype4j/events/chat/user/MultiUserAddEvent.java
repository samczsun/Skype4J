package com.samczsun.skype4j.events.chat.user;

import com.samczsun.skype4j.user.User;

import java.util.Collections;
import java.util.List;

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

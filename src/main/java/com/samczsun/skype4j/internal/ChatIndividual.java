package com.samczsun.skype4j.internal;

import com.samczsun.skype4j.chat.IndividualChat;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.user.User;

import java.util.HashMap;
import java.util.Map;

public class ChatIndividual extends ChatImpl implements IndividualChat {
    private User partner;

    protected ChatIndividual(SkypeImpl skype, String identity) throws ConnectionException, ChatNotFoundException {
        super(skype, identity);
    }

    @Override
    protected void load() throws ConnectionException {
        if (isLoaded()) {
            return;
        }
        isLoading.set(true);
        Map<String, User> newUsers = new HashMap<>();
        String username = this.getIdentity().substring(2);
        User user = users.get(username.toLowerCase());
        if (user == null) {
            user = new UserImpl(username, this);
        }
        newUsers.put(username.toLowerCase(), user);
        this.partner = user;
        User me = users.get(getClient().getUsername().toLowerCase());
        if (me == null) {
            me = new UserImpl(getClient().getUsername(), this);
            newUsers.put(getClient().getUsername().toLowerCase(), me);
        }
        this.users.clear();
        this.users.putAll(newUsers);
        isLoading.set(false);
        hasLoaded.set(true);
    }

    public void addUser(String username) {
        throw new IllegalArgumentException("Cannot add user to individual chat");
    }

    public void removeUser(String username) {
        throw new IllegalArgumentException("Cannot remove user from individual chat");
    }

    @Override
    public User getPartner() {
        return partner;
    }
}

/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
 *
 * This file is part of Skype4J.
 *
 * Skype4J is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Skype4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Skype4J.
 * If not, see http://www.gnu.org/licenses/.
 */

package com.samczsun.skype4j.internal.chat;

import com.samczsun.skype4j.chat.IndividualChat;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.UserImpl;
import com.samczsun.skype4j.user.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatIndividual extends ChatImpl implements IndividualChat {
    private User partner;

    protected ChatIndividual(SkypeImpl skype, String identity) throws ConnectionException, ChatNotFoundException, IOException {
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

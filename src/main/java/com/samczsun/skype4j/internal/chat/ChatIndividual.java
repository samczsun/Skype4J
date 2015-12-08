/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.samczsun.skype4j.internal.chat;

import com.samczsun.skype4j.chat.IndividualChat;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.UserImpl;
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
        Map<String, UserImpl> newUsers = new HashMap<>();
        String username = this.getIdentity().substring(2);
        UserImpl user = users.get(username.toLowerCase());
        if (user == null) {
            user = new UserImpl(username, this, getClient());
        }
        newUsers.put(username.toLowerCase(), user);
        this.partner = user;
        UserImpl me = users.get(getClient().getUsername().toLowerCase());
        if (me == null) {
            me = new UserImpl(getClient().getUsername(), this, getClient());
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

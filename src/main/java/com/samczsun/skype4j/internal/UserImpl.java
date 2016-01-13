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

package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NoPermissionException;
import com.samczsun.skype4j.internal.chat.ChatImpl;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserImpl implements User {

    private final String username;
    private final ChatImpl chat;
    private final SkypeImpl client;

    private Role role = Role.USER;
    private Contact contactRep; //Lazily loaded

    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    private final Map<String, ChatMessage> messageMap = new ConcurrentHashMap<>();

    public UserImpl(String username, ChatImpl chat, SkypeImpl client) throws ConnectionException {
        this.username = username;
        this.chat = chat;
        this.client = client;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getDisplayName() throws ConnectionException {
        loadContact();
        return contactRep.getDisplayName();
    }

    @Override
    public Contact getContact() throws ConnectionException {
        loadContact();
        return this.contactRep;
    }

    @Override
    public Role getRole() {
        return this.role;
    }

    @Override
    public void setRole(Role role) throws ConnectionException, NoPermissionException {
        if (!(getChat() instanceof GroupChat)) throw new NoPermissionException();
        Endpoints.MODIFY_MEMBER_URL.open(getClient(), getChat().getIdentity(), getUsername()).on(400, (connection) -> {
            throw new NoPermissionException();
        }).expect(200, "While updating role").put(new JsonObject().add("role", role.name().toLowerCase()));
        updateRole(role);
    }

    @Override
    public SkypeImpl getClient() {
        return this.client;
    }


    @Override
    public Chat getChat() {
        return this.chat;
    }

    @Override
    public List<ChatMessage> getSentMessages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public ChatMessage getMessageById(String id) {
        return messageMap.get(id);
    }

    public void onMessage(ChatMessage message) {
        this.messages.add(message);
        this.messageMap.put(message.getClientId(), message);
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void insertMessage(ChatMessage m, int i) {
        this.messages.add(i, m);
    }

    private void loadContact() throws ConnectionException {
        if (this.contactRep == null) {
            this.contactRep = chat.getClient().getOrLoadContact(username);
        }
    }
}

/*
 * Copyright 2016 Sam Sun <me@samczsun.com>
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

package com.samczsun.skype4j.internal.participants;

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NoPermissionException;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.chat.ChatImpl;
import com.samczsun.skype4j.participants.Participant;

import java.util.*;

public abstract class ParticipantImpl implements Participant {

    private SkypeImpl skype;
    private String id;

    private ChatImpl chat;

    private Participant.Role role = Participant.Role.USER;

    private final List<ChatMessage> messages = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, ChatMessage> messageMap = Collections.synchronizedMap(new HashMap<>());

    public ParticipantImpl(SkypeImpl skype, ChatImpl chat, String id) {
        this.skype = skype;
        this.id = id;
        this.chat = chat;
    }

    @Override
    public SkypeImpl getClient() {
        return skype;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public abstract String getDisplayName();

    @Override
    public Chat getChat() {
        return chat;
    }

    @Override
    public List<ChatMessage> getSentMessages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public ChatMessage getMessageById(String id) {
        return messageMap.get(id);
    }

    @Override
    public Participant.Role getRole() {
        return this.role;
    }

    @Override
    public void setRole(Participant.Role role) throws ConnectionException, NoPermissionException {
        if (!(getChat() instanceof GroupChat)) throw new NoPermissionException();
        Endpoints.MODIFY_MEMBER_URL.open(getClient(), getChat().getIdentity(), getId()).on(400, (connection) -> {
            throw new NoPermissionException();
        }).expect(200, "While updating role").put(new JsonObject().add("role", role.name().toLowerCase()));
        updateRole(role);
    }

    public void updateRole(Participant.Role role) {
        this.role = role;
    }

    public void onMessage(ChatMessage message) {
        this.messages.add(message);
        this.messageMap.put(message.getClientId(), message);
    }

    public void insertMessage(ChatMessage m, int i) {
        this.messages.add(i, m);
        this.messageMap.put(m.getClientId(), m);
    }
}

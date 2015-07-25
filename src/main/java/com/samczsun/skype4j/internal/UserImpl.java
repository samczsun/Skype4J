package com.samczsun.skype4j.internal;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserImpl implements User {

    private final Contact contactRep;
    private final ChatImpl chat;

    private Role role = Role.USER;

    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    private final Map<String, ChatMessage> messageMap = new ConcurrentHashMap<>();

    public UserImpl(String username, ChatImpl chat) throws ConnectionException {
        this.contactRep = chat.getClient().getOrLoadContact(username);
        this.chat = chat;
    }

    @Override
    public String getUsername() {
        return contactRep.getUsername();
    }

    @Override
    public String getDisplayName() {
        return contactRep.getDisplayName();
    }

    @Override
    public Role getRole() {
        return this.role;
    }

    @Override
    public void setRole(Role role) {
        this.role = role;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserImpl user = (UserImpl) o;

        if (!contactRep.equals(user.contactRep)) return false;
        if (!chat.equals(user.chat)) return false;
        return role == user.role;

    }

    @Override
    public int hashCode() {
        int result = contactRep.hashCode();
        result = 31 * result + chat.hashCode();
        result = 31 * result + role.hashCode();
        return result;
    }
}

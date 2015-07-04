package com.samczsun.skype4j.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.user.User;

public class UserImpl implements User {
    private String username;

    private final Chat chat;
    private Role role = Role.USER;
    
    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    private final Map<String, ChatMessage> messageMap = new ConcurrentHashMap<>();

    public UserImpl(String username, Chat chat) {
        this.username = username;
        this.chat = chat;
    }

    public UserImpl(Chat chat) {
        this.chat = chat;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getDisplayName() {
        return null;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserImpl other = (UserImpl) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
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
}

package com.samczsun.skype4j.user;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;

import java.util.List;

public interface User {
    String getUsername();

    String getDisplayName();

    Role getRole();

    void setRole(Role role);

    Chat getChat();

    List<ChatMessage> getSentMessages();

    ChatMessage getMessageById(String id);

    enum Role {
        ADMIN, USER;

        public static Role getByName(String name) {
            return name.equalsIgnoreCase("admin") ? ADMIN : USER;
        }
    }
}

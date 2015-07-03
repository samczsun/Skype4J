package com.samczsun.skype4j.user;

import java.util.List;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;

public interface User {
    public String getUsername();

    public String getDisplayName();

    public Role getRole();

    public void setRole(Role role);

    public Chat getChat();
    
    public List<ChatMessage> getSentMessages();
    
    public ChatMessage getMessageById(String id);

    public static enum Role {
        ADMIN, USER;
        
        public static Role getByName(String name) {
            return name.equalsIgnoreCase("admin") ? ADMIN : USER;
        }
    }
}

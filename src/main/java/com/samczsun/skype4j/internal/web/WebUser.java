package com.samczsun.skype4j.internal.web;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.User;

public class WebUser implements User {
    private String username;

    private Chat chat;
    private Role role = Role.USER;

    public WebUser(String username, Chat chat) {
        this.username = username;
        this.chat = chat;
    }

    public WebUser(Chat chat) {
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
        WebUser other = (WebUser) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }
}

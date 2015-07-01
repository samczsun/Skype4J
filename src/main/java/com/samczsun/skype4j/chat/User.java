package com.samczsun.skype4j.chat;


public interface User {
    public String getUsername();

    public String getDisplayName();

    public Role getRole();

    public void setRole(Role role);

    public Chat getChat();

    public static enum Role {
        ADMIN, USER;

        public boolean isAdmin() {
            return this == ADMIN;
        }
    }
}

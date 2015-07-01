package com.samczsun.skype4j;

public class SkypeClient {
    
    public static PendingLogin create(String username, String password) {
        return PendingLogin.create(username, password);
    }
}

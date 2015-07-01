package com.samczsun.skype4j;

import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.internal.web.WebSkype;


public class PendingLogin {
    private String username;
    private String password;
    private Client clientType;

    private PendingLogin(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public PendingLogin client(Client newClient) {
        this.clientType = newClient;
        return this;
    }
    
    public Skype login() throws SkypeException {
        switch (clientType) {
        case WEB:
            return new WebSkype(username, password);
        }
        throw new IllegalArgumentException("Unknown client type");
    }

    public static PendingLogin create(String username, String password) {
        return new PendingLogin(username, password);
    }
    
    public static enum Client {
        WEB;
    }
}

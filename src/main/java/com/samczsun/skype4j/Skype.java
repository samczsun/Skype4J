package com.samczsun.skype4j;

import java.io.IOException;
import java.util.List;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.EventDispatcher;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.internal.SkypeImpl;

public abstract class Skype {

    /**
     * If this Skype client is web-based, this method will subscribe to the
     * notifications and events will begin. Not sure what this will do on
     * desktop-based clients yet.
     * 
     * @throws IOException
     *             Thrown if any internal operations go wrong
     */
    public abstract void subscribe() throws IOException;

    public abstract String getUsername();

    public abstract Chat getChat(String name);

    public abstract List<Chat> getAllChats();

    public abstract void logout() throws IOException;

    public abstract EventDispatcher getEventDispatcher();
    
    public static Skype login(String username, String password) throws SkypeException {
        return new SkypeImpl(username, password);
    }
}

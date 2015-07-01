package com.samczsun.skype4j;

import java.io.IOException;
import java.util.List;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.EventDispatcher;

public interface Skype {

    /**
     * If this Skype client is web-based, this method will subscribe to the
     * notifications and events will begin. Not sure what this will do on
     * desktop-based clients yet.
     * 
     * @throws IOException
     *             Thrown if any internal operations go wrong
     */
    public void subscribe() throws IOException;

    public String getUsername();

    public Chat getChat(String name);

    public List<Chat> getAllChats();

    public void logout() throws IOException;

    public EventDispatcher getEventDispatcher();
}

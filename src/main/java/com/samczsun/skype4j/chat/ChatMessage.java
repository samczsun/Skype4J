package com.samczsun.skype4j.chat;

import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.RichText;
import com.samczsun.skype4j.user.User;

/**
 * Represents a single message sent in a group
 * 
 * @author samczsun
 */
public interface ChatMessage {
    
    String getClientId();

    Message getMessage();

    long getTime();

    User getSender();

    void edit(Message newMessage) throws SkypeException;

    void delete() throws SkypeException;

    Chat getChat();

    String getId();
}

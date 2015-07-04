package com.samczsun.skype4j.chat;

import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Text;
import com.samczsun.skype4j.user.User;

/**
 * Represents a single message sent in a group
 * 
 * @author samczsun
 */
public interface ChatMessage {
    
    String getClientId();

    String getText();

    long getTime();

    User getSender();

    void edit(Text newMessage) throws SkypeException;

    void delete() throws SkypeException;

    Chat getChat();

    String getId();
}

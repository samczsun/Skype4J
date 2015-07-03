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
    
    public String getClientId();

    public String getText();

    public long getTime();

    public User getSender();

    public void edit(Text newMessage) throws SkypeException;

    public void delete() throws SkypeException;

    public Chat getChat();

    public String getId();
}

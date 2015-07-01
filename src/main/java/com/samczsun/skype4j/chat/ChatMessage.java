package com.samczsun.skype4j.chat;

import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Text;

public interface ChatMessage {
    public String getClientId();

    public String getMessage();

    public long getTime();

    public User getSender();

    public void edit(Text newMessage) throws SkypeException;

    public Chat getChat();
}

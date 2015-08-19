package com.samczsun.skype4j.events;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User;

public class UnsupportedEvent extends Event
{
    private String name;
    private final String content;


    public UnsupportedEvent(String name, String content)
    {
        super();
        this.name = name;
        this.content = content;
    }

    public String getName() { return this.name; }

    public String getContent() { return this.content; }
}

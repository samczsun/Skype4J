package com.samczsun.skype4j.events.chat.sent;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User;

public class TypingReceivedEvent extends ChatEvent 
{
    private User sender;
	private boolean started;

    public TypingReceivedEvent(Chat chat, User sender, boolean started) 
	{
        super(chat);
        this.sender = sender;
		this.started = started;
    }

    public User getSender()	{
        return this.sender;
    }

	public boolean getStarted()
	{
		return this.started;
	}

}
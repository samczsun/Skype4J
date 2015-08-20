package com.samczsun.skype4j.events.chat.call;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User;

public class CallReceivedEvent extends ChatEvent 
{
    private User sender;
	private boolean callStarted;

    public CallReceivedEvent(Chat chat, User sender, boolean callStarted) 
	{
        super(chat);
        this.sender = sender;
		this.callStarted = callStarted;
    }

    public User getSender() 
	{
        return this.sender;
    }

	public boolean getCallStarted() 
	{
        return this.callStarted;
    }
}

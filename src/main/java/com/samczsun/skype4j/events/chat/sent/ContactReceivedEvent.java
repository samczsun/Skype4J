package com.samczsun.skype4j.events.chat.sent;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User;

import java.util.Iterator;

public class ContactReceivedEvent extends ChatEvent {
    private User sender;
    private Contact sentContact;

    public ContactReceivedEvent(Chat chat, User sender, Contact sent) {
        super(chat);
        this.sender = sender;
        this.sentContact = sent;
    }

    public User getSender()
    {
        return this.sender;
    }

    public Contact getSentContact()
    {
        return this.sentContact;
    }
}

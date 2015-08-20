package com.samczsun.skype4j.events.chat.sent;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User;

import java.util.Iterator;

public class ContactReceivedEvent extends ChatEvent {
    private User sender;
    private Iterable<Contact> sentContacts;

    public ContactReceivedEvent(Chat chat, User sender, Iterable<Contact> sent) {
        super(chat);
        this.sender = sender;
        this.sentContacts = sent;
    }

    public User getSender()
    {
        return this.sender;
    }

    @Deprecated
    public Contact getSentContact()
    {
        Iterator<Contact> i = this.sentContacts.iterator();
        return i.hasNext() ? this.sentContacts.iterator().next() : null;
    }

    public Iterable<Contact> getSentContacts()
    {
        return this.sentContacts;
    }
}

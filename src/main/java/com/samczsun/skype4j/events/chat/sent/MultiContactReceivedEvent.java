package com.samczsun.skype4j.events.chat.sent;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User;

import java.util.Iterator;
import java.util.List;

public class MultiContactReceivedEvent extends ContactReceivedEvent {
    private User sender;
    private List<Contact> sentContacts;

    public MultiContactReceivedEvent(Chat chat, User sender, List<Contact> sent) {
        super(chat, sender, sent.get(0));
        this.sender = sender;
        this.sentContacts = sent;
    }

    public User getSender()
    {
        return this.sender;
    }

    public Iterable<Contact> getSentContacts()
    {
        return this.sentContacts;
    }
}

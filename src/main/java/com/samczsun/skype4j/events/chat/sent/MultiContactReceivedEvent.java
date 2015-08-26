package com.samczsun.skype4j.events.chat.sent;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User;

import java.util.Iterator;
<<<<<<< HEAD

public class MultiContactReceivedEvent extends ChatEvent {
    private User sender;
    private Iterable<Contact> sentContacts;

    public MultiContactReceivedEvent(Chat chat, User sender, Iterable<Contact> sent) {
        super(chat);
=======
import java.util.List;

public class MultiContactReceivedEvent extends ContactReceivedEvent {
    private User sender;
    private List<Contact> sentContacts;

    public MultiContactReceivedEvent(Chat chat, User sender, List<Contact> sent) {
        super(chat, sender, sent.get(0));
>>>>>>> a23dcc18cd2d1774b1bd34a29eb3e5a9f18a854f
        this.sender = sender;
        this.sentContacts = sent;
    }

    public User getSender()
    {
        return this.sender;
    }

<<<<<<< HEAD
    /*
    @Deprecated
    public Contact getSentContact()
    {
        Iterator<Contact> i = this.sentContacts.iterator();
        return i.hasNext() ? this.sentContacts.iterator().next() : null;
    }
    */

=======
>>>>>>> a23dcc18cd2d1774b1bd34a29eb3e5a9f18a854f
    public Iterable<Contact> getSentContacts()
    {
        return this.sentContacts;
    }
}

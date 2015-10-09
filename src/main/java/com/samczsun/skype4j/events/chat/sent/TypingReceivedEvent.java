package com.samczsun.skype4j.events.chat.sent;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.user.User;

public class TypingReceivedEvent extends ChatEvent {
    private User sender;
    private boolean typing;

    public TypingReceivedEvent(Chat chat, User sender, boolean typing) {
        super(chat);
        this.sender = sender;
        this.typing = typing;
    }

    public User getSender() {
        return this.sender;
    }

    public boolean isTyping() {
        return this.typing;
    }

}

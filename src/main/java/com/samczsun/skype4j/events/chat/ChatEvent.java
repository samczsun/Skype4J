package com.samczsun.skype4j.events.chat;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.Event;

public abstract class ChatEvent extends Event {
    private final Chat chat;

    public ChatEvent(Chat c) {
        this.chat = c;
    }

    public Chat getChat() {
        return chat;
    }
}

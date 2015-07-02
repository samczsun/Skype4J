package com.samczsun.skype4j.events.chat;

import com.samczsun.skype4j.chat.Chat;

public class ChatJoinedEvent extends ChatEvent {
    public ChatJoinedEvent(Chat c) {
        super(c);
    }
}

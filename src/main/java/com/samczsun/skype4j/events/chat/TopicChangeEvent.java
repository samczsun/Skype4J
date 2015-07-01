package com.samczsun.skype4j.events.chat;

import com.samczsun.skype4j.chat.Chat;

public class TopicChangeEvent extends ChatEvent {
    public TopicChangeEvent(Chat c) {
        super(c);
    }
}

package com.samczsun.skype4j.events.chat.message;

import com.samczsun.skype4j.chat.ChatMessage;

public class MessageEditedEvent extends MessageEvent {
    public MessageEditedEvent(ChatMessage message) {
        super(message);
    }
}

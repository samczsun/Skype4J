package com.samczsun.skype4j.events.chat.message;

import com.samczsun.skype4j.chat.ChatMessage;

public class MessageReceivedEvent extends MessageEvent {
    public MessageReceivedEvent(ChatMessage message) {
        super(message);
    }
}

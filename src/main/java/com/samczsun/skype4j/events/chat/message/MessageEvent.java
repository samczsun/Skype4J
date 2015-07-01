package com.samczsun.skype4j.events.chat.message;

import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.events.chat.ChatEvent;

public abstract class MessageEvent extends ChatEvent {
    private ChatMessage message;

    public MessageEvent(ChatMessage message) {
        super(message.getChat());
        this.message = message;
    }
    
    public ChatMessage getMessage() {
        return this.message;
    }
}

package com.samczsun.skype4j.events.chat.message;

import com.samczsun.skype4j.chat.ChatMessage;

public class MessageEditedEvent extends MessageEvent {
    private String newContent;

    public MessageEditedEvent(ChatMessage message, String newContent) {
        super(message);
        this.newContent = newContent;
    }
    
    public String getNewContent() {
        return this.newContent;
    }
}

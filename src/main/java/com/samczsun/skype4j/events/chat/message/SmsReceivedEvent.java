package com.samczsun.skype4j.events.chat.message;

import com.samczsun.skype4j.chat.ChatMessage;

public class SmsReceivedEvent extends MessageReceivedEvent {
    public SmsReceivedEvent(ChatMessage message) {
        super(message);
    }
}

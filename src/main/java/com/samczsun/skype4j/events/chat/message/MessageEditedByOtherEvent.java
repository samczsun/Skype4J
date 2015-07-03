package com.samczsun.skype4j.events.chat.message;

import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.user.User;

public class MessageEditedByOtherEvent extends MessageEvent {
    private User malicious;
    private String newContent;

    public MessageEditedByOtherEvent(ChatMessage message, String newContent, User user) {
        super(message);
        this.malicious = user;
        this.newContent = newContent;
    }

    public User getMaliciousUser() {
        return this.malicious;
    }

    public String getNewContent() {
        return this.newContent;
    }
}

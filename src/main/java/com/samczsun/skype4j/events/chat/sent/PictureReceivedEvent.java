package com.samczsun.skype4j.events.chat.sent;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.user.User;

import java.awt.image.BufferedImage;

public class PictureReceivedEvent extends ChatEvent {
    private User sender;
    private String originalName;
    private BufferedImage sentImage;

    public PictureReceivedEvent(Chat chat, User sender, String originalName, BufferedImage sent) {
        super(chat);
        this.sender = sender;
        this.originalName = originalName;
        this.sentImage = sent;
    }

    public User getSender() {
        return this.sender;
    }

    public BufferedImage getSentImage() {
        return this.sentImage;
    }

    public String getOriginalName() {
        return this.originalName;
    }
}

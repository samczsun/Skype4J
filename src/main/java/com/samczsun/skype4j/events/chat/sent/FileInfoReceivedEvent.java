package com.samczsun.skype4j.events.chat.sent;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.FileInfo;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.user.User;

import java.awt.image.BufferedImage;

public class FileInfoReceivedEvent extends ChatEvent
{
    private User sender;
    private Iterable<FileInfo> sentFileInfos;

    public FileInfoReceivedEvent(Chat chat, User sender, Iterable<FileInfo> sent) {
        super(chat);
        this.sender = sender;
        this.sentFileInfos = sent;
    }

    public User getSender() {
        return this.sender;
    }

    public Iterable<FileInfo> getSentFiles() { return this.sentFileInfos; }
}

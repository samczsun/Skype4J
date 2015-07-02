package com.samczsun.skype4j.internal.web;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.chat.User;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Text;

public class WebOtherChatMessage implements ChatMessage {
    private String clientId;
    private String message;
    private long time;
    private User sender;

    public WebOtherChatMessage(Chat chat, User user, String id, String clientId, long time, String message) {
        this.clientId = clientId;
        this.message = message;
        this.time = time;
        this.sender = user;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getText() {
        return message;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public User getSender() {
        return sender;
    }

    @Override
    public void edit(Text newMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Chat getChat() {
        return sender.getChat();
    }

    @Override
    public void delete() throws SkypeException {
        throw new UnsupportedOperationException();
    }
}

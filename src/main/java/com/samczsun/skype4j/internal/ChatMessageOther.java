    package com.samczsun.skype4j.internal;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Text;
import com.samczsun.skype4j.user.User;

public class ChatMessageOther extends ChatMessageImpl {
    private final String clientId;
    private final String id;
    private String message;
    private final long time;
    private final User sender;

    public ChatMessageOther(Chat chat, User user, String id, String clientId, long time, String message) {
        this.clientId = clientId;
        this.message = message;
        this.time = time;
        this.sender = user;
        this.id = id;
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

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setContent(String content) {
        this.message = content;
    }
}

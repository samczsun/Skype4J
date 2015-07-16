package com.samczsun.skype4j.internal;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ReceivedMessage;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.user.User;

public class ChatMessageOther extends ChatMessageImpl implements ReceivedMessage {
    private final String clientId;
    private final String id;
    private Message message;
    private final long time;
    private final User sender;

    public ChatMessageOther(Chat chat, User user, String id, String clientId, long time, Message message) {
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
    public Message getMessage() {
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
    public Chat getChat() {
        return sender.getChat();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setContent(Message content) {
        this.message = content;
    }
}

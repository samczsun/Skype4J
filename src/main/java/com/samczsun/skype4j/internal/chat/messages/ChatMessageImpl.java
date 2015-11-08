/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
 *
 * This file is part of Skype4J.
 *
 * Skype4J is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Skype4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Skype4J.
 * If not, see http://www.gnu.org/licenses/.
 */

package com.samczsun.skype4j.internal.chat.messages;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.UserImpl;
import com.samczsun.skype4j.internal.chat.ChatImpl;
import com.samczsun.skype4j.user.User;
import org.jsoup.helper.Validate;

public abstract class ChatMessageImpl implements ChatMessage {
    private Chat chat;
    private User sender;
    private String clientId;
    private String id;
    private Message message;
    private long time;
    private SkypeImpl skype;

    public ChatMessageImpl(Chat chat, User sender, String id, String clientId, long time, Message message, SkypeImpl skype) {
        this.chat = chat;
        this.sender = sender;
        this.id = id;
        this.clientId = clientId;
        this.time = time;
        this.message = message;
        this.skype = skype;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public User getSender() {
        return this.sender;
    }

    @Override
    public Message getContent() {
        return message;
    }

    @Override
    public long getSentTime() {
        return time;
    }

    @Override
    public Chat getChat() {
        return chat;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public SkypeImpl getClient() {
        return this.skype;
    }

    public static ChatMessage createMessage(Chat chat, User user, String id, String clientId, long time, Message message, SkypeImpl skype) {
        Validate.notNull(chat, "Chat must not be null");
        Validate.isTrue(chat instanceof ChatImpl, "Chat must be instanceof ChatImpl");
        Validate.notNull(user, "User must not be null");
        Validate.isTrue(user instanceof UserImpl, "User must be instanceof UserImpl");
        Validate.notNull(message, "Message must not be null");
        if (((ChatImpl) chat).getClient().getUsername().equals(user.getUsername())) {
            return new SentMessageImpl(chat, user, id, clientId, time, message, skype);
        } else {
            return new ReceivedMessageImpl(chat, user, id, clientId, time, message, skype);
        }
    }

    public void edit0(Message newMessage) {
        this.message = newMessage;
    }
}

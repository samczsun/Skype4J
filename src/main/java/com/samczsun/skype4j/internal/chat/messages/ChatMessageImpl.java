/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.samczsun.skype4j.internal.chat.messages;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.exceptions.ConnectionException;
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

    public static ChatMessage createMessage(Chat chat, User user, String id, String clientId, long time, Message message, SkypeImpl skype) throws ConnectionException {
        Validate.notNull(chat, "Chat must not be null");
        Validate.isTrue(chat instanceof ChatImpl, "Chat must be instanceof ChatImpl");
        Validate.notNull(user, "User must not be null");
        Validate.isTrue(user instanceof UserImpl, "User must be instanceof UserImpl");
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

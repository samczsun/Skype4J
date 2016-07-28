/*
 * Copyright 2016 Sam Sun <me@samczsun.com>
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
import com.samczsun.skype4j.internal.chat.ChatImpl;
import com.samczsun.skype4j.internal.participants.ParticipantImpl;
import com.samczsun.skype4j.internal.participants.UserImpl;
import com.samczsun.skype4j.participants.Participant;
import com.samczsun.skype4j.participants.User;
import org.jsoup.helper.Validate;

public abstract class ChatMessageImpl implements ChatMessage {

    private final String clientId;
    private final SkypeImpl skype;

    private final ParticipantImpl sender;
    private final Chat chat;
    private final String id;
    private final long time;

    private Message message;

    public ChatMessageImpl(Chat chat, ParticipantImpl sender, String id, String clientId, long time, Message message, SkypeImpl skype) {
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
    public ParticipantImpl getSender() {
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

    public void edit0(Message newMessage) {
        this.message = newMessage;
    }
}

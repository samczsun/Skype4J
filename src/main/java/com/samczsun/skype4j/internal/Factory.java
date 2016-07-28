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

package com.samczsun.skype4j.internal;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.chat.*;
import com.samczsun.skype4j.internal.chat.messages.ChatMessageImpl;
import com.samczsun.skype4j.internal.chat.messages.ReceivedMessageImpl;
import com.samczsun.skype4j.internal.chat.messages.SentMessageImpl;
import com.samczsun.skype4j.internal.participants.BotImpl;
import com.samczsun.skype4j.internal.participants.ParticipantImpl;
import com.samczsun.skype4j.internal.participants.UserImpl;
import com.samczsun.skype4j.participants.Participant;
import org.jsoup.helper.Validate;

public class Factory {

    public static ChatImpl createChat(SkypeImpl client, String identity) throws ConnectionException, ChatNotFoundException {
        Validate.notNull(client, "Client must not be null");
        Validate.notEmpty(identity, "Identity must not be null/empty");

        ChatImpl result = null;

        if (identity.startsWith("19:")) {
            if (identity.endsWith("@thread.skype")) {
                result = new ChatGroup(client, identity);
            } else if (identity.endsWith("@p2p.thread.skype")) {
                result = new ChatP2P(client, identity);
            }
        } else if (identity.startsWith("8:")) {
            result = new ChatIndividual(client, identity);
        } else if (identity.startsWith("28:")) {
            result = new ChatBot(client, identity);
        }

        if (result != null) {
            result.load();
            return result;
        }

        throw new IllegalArgumentException(String.format("Unknown chat type with identity %s", identity));
    }

    public static ParticipantImpl createParticipant(SkypeImpl client, ChatImpl chat, String id) throws ConnectionException {
        Validate.notNull(client, "Client must not be null");
        Validate.notNull(chat, "Chat must not be null");
        Validate.notEmpty(id, "Identity must not be null/empty");

        ParticipantImpl result = null;

        if (id.startsWith("8:")) {
            result = new UserImpl(client, chat, id);
        } else if (id.startsWith("28:")) {
            result = new BotImpl(client, chat, id);
        }

        if (result != null) {
            return result;
        }

        throw new IllegalArgumentException(String.format("Unknown participant type with id %s", id));
    }

    public static ChatMessageImpl createMessage(Chat chat, ParticipantImpl user, String id, String clientId, long time, Message message, SkypeImpl skype) throws ConnectionException {
        Validate.notNull(chat, "Chat must not be null");
        Validate.isTrue(chat instanceof ChatImpl, "Chat must be instanceof ChatImpl");
        Validate.notNull(user, "User must not be null");

        if (("8:" + chat.getClient().getUsername()).equals(user.getId())) {
            return new SentMessageImpl(chat, user, id, clientId, time, message, skype);
        } else {
            return new ReceivedMessageImpl(chat, user, id, clientId, time, message, skype);
        }
    }
}

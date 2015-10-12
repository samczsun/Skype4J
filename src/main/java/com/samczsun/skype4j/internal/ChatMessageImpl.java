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

package com.samczsun.skype4j.internal;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.user.User;
import org.jsoup.helper.Validate;

public abstract class ChatMessageImpl implements ChatMessage {
    public static ChatMessage createMessage(Chat chat, User user, String id, String clientId, long time, Message message) {
        Validate.notNull(chat, "Chat must not be null");
        Validate.isTrue(chat instanceof ChatImpl, "Chat must be instanceof ChatImpl");
        Validate.notNull(user, "User must not be null");
        Validate.isTrue(user instanceof UserImpl, "User must be instanceof UserImpl");
        Validate.notNull(message, "Message must not be null");
        if (((ChatImpl) chat).getClient().getUsername().equals(user.getUsername())) {
            return new ChatMessageSelf(chat, user, id, clientId, time, message);
        } else {
            return new ChatMessageOther(chat, user, id, clientId, time, message);
        }
    }

    public abstract void setContent(Message content);
}

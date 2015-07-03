package com.samczsun.skype4j.internal;

import org.jsoup.helper.Validate;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.user.User;

public abstract class ChatMessageImpl implements ChatMessage {
    public static ChatMessage createMessage(Chat chat, User user, String id, String clientId, long time, String message) {
        Validate.notNull(chat, "Chat must not be null");
        Validate.isTrue(chat instanceof ChatImpl, "Chat must be instanceof WebChat");
        Validate.notNull(chat, "User must not be null");
        Validate.isTrue(user instanceof UserImpl, "User must be instanceof WebUser");
        Validate.notEmpty(clientId, "ClientId must not be null");
        Validate.notEmpty(message, "Message must not be null");
        if (((ChatImpl) chat).getClient().getUsername().equals(user.getUsername())) {
            return new ChatMessageSelf(chat, user, id, clientId, time, message);
        } else {
            return new ChatMessageOther(chat, user, id, clientId, time, message);
        }
    }

    public abstract void setContent(String content);
}

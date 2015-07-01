package com.samczsun.skype4j.internal.web;

import org.jsoup.helper.Validate;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.chat.User;

public class WebChatMessage {
    public static ChatMessage createMessage(Chat chat, User user, String id, String clientId, long time, String message) {
        Validate.notNull(chat, "Chat must not be null");
        Validate.isTrue(chat instanceof WebChat, "Chat must be instanceof WebChat");
        Validate.notNull(chat, "User must not be null");
        Validate.isTrue(user instanceof WebUser, "User must be instanceof WebUser");
        Validate.notEmpty(clientId, "ClientId must not be null");
        Validate.notEmpty(message, "Message must not be null");
        if (((WebChat) chat).getClient().getUsername().equals(user.getUsername())) {
            return new WebSelfChatMessage(chat, user, id, clientId, time, message);
        } else {
            return new WebOtherChatMessage(chat, user, id, clientId, time, message);
        }
    }
}

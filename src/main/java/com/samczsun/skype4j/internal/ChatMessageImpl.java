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

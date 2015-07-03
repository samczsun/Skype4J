package com.samczsun.skype4j.internal;

import org.jsoup.helper.Validate;

import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;

public abstract class ChatImpl implements Chat {
    public static Chat createChat(Skype client, String identity) {
        Validate.notNull(client, "Client must not be null");
        Validate.isTrue(client instanceof SkypeImpl, "Client type must be Web");
        Validate.notEmpty(identity, "Identity must not be empty");
        if (identity.startsWith("19:") && identity.endsWith("@thread.skype")) {
            return new ChatGroup((SkypeImpl) client, identity);
        } else if (identity.startsWith("8:")) {
            return new ChatIndividual((SkypeImpl) client, identity);
        } else {
            throw new IllegalArgumentException(String.format("Unknown group type with identity %s", identity));
        }
    }

    private final SkypeImpl client;
    private final String identity;

    public ChatImpl(SkypeImpl client, String identity) {
        this.client = client;
        this.identity = identity;
    }

    public SkypeImpl getClient() {
        return this.client;
    }

    public String getIdentity() {
        return this.identity;
    }

    public abstract void addUser(String username);

    public abstract void removeUser(String username);

    public abstract void onMessage(ChatMessage m);
}

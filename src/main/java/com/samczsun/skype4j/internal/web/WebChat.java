package com.samczsun.skype4j.internal.web;

import org.jsoup.helper.Validate;

import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.chat.Chat;

public abstract class WebChat implements Chat {
    public static Chat createChat(Skype client, String identity) {
        Validate.notNull(client, "Client must not be null");
        Validate.isTrue(client instanceof WebSkype, "Client type must be Web");
        Validate.notEmpty(identity, "Identity must not be empty");
        if (identity.startsWith("19:") && identity.endsWith("@thread.skype")) {
            return new WebChatGroup((WebSkype) client, identity);
        } else if (identity.startsWith("8:")) {
            return new WebChatIndividual((WebSkype) client, identity);
        } else {
            throw new IllegalArgumentException(String.format("Unknown group type with identity %s", identity));
        }
    }

    private final WebSkype client;
    private final String identity;

    public WebChat(WebSkype client, String identity) {
        this.client = client;
        this.identity = identity;
    }

    public WebSkype getClient() {
        return this.client;
    }

    public String getIdentity() {
        return this.identity;
    }

    public abstract void addUser(String username);

    public abstract void removeUser(String username);
}

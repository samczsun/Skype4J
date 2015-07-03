package com.samczsun.skype4j.internal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jsoup.helper.Validate;

import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.exceptions.NotLoadedException;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.user.User;

public abstract class ChatImpl {
    public static Chat createChat(Skype client, String identity) throws SkypeException {
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

    protected final AtomicBoolean isLoading = new AtomicBoolean(false);
    protected final AtomicBoolean hasLoaded = new AtomicBoolean(false);

    private final SkypeImpl client;
    private final String identity;

    protected final Map<String, User> users = new ConcurrentHashMap<>();
    protected final List<ChatMessage> messages = new CopyOnWriteArrayList<>();

    public ChatImpl(SkypeImpl client, String identity) throws SkypeException {
        this.client = client;
        this.identity = identity;
        load();
    }

    public SkypeImpl getClient() {
        return this.client;
    }

    public String getIdentity() {
        return this.identity;
    }

    public boolean isLoaded() {
        return !isLoading.get() && hasLoaded.get();
    }

    public abstract void addUser(String username);

    public abstract void removeUser(String username);

    public abstract void onMessage(ChatMessage m);

    protected abstract void load() throws SkypeException;
    
    protected void checkLoaded() throws NotLoadedException {
        if (!isLoaded()) {
            throw new NotLoadedException();
        }
    }
}

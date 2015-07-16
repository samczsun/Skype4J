package com.samczsun.skype4j.internal;

import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NotLoadedException;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.user.User;
import org.jsoup.helper.Validate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ChatImpl implements Chat {
    public static Chat createChat(Skype client, String identity) throws SkypeException {
        Validate.notNull(client, "Client must not be null");
        Validate.isTrue(client instanceof SkypeImpl, String.format("Now is not the time to use that, %s", client.getUsername()));
        Validate.notEmpty(identity, "Identity must not be null/empty");
        if (identity.startsWith("19:")) {
            if (identity.endsWith("@thread.skype")) {
                return new ChatGroup((SkypeImpl) client, identity);
            } else {
                client.getLogger().info(String.format("Skipping P2P chat with identity %s", identity));
                return null;
            }
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

    ChatImpl(SkypeImpl client, String identity) throws ConnectionException {
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

    protected abstract void load() throws ConnectionException;

    protected void checkLoaded() throws NotLoadedException {
        if (!isLoaded()) {
            throw new NotLoadedException();
        }
    }
}

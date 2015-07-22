package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.ConnectionBuilder;
import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NotLoadedException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.user.User;
import org.jsoup.helper.Validate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ChatImpl implements Chat {
    protected static final String CHAT_INFO_URL = "https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/?view=msnp24Equivalent";
    protected static final String SEND_MESSAGE_URL = "https://%sclient-s.gateway.messenger.live.com/v1/users/ME/conversations/%s/messages";
    protected static final String MODIFY_MEMBER_URL = "https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/members/8:%s";
    protected static final String MODIFY_PROPERTY_URL = "https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/properties?name=%s";

    public static Chat createChat(Skype client, String identity) throws ConnectionException {
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

    @Override
    public ChatMessage sendMessage(Message message) throws ConnectionException {
        checkLoaded();
        try {
            long ms = System.currentTimeMillis();
            JsonObject obj = new JsonObject();
            obj.add("content", message.write());
            obj.add("messagetype", "RichText");
            obj.add("contenttype", "text");
            obj.add("clientmessageid", String.valueOf(ms));
            ConnectionBuilder builder = new ConnectionBuilder();
            builder.setUrl(getClient().withCloud(SEND_MESSAGE_URL, getIdentity()));
            builder.setMethod("POST", true);
            builder.addHeader("RegistrationToken", getClient().getRegistrationToken());
            builder.addHeader("Content-Type", "application/json");
            builder.setData(obj.toString());
            HttpURLConnection con = builder.build();
            if (con.getResponseCode() == 201) {
                return ChatMessageImpl.createMessage(this, getUser(getClient().getUsername()), null, String.valueOf(ms), ms, message);
            } else {
                throw getClient().generateException(con);
            }
        } catch (IOException e) {
            throw new ConnectionException("While sending a message", e);
        }
    }

    @Override
    public Collection<User> getAllUsers() throws NotLoadedException {
        checkLoaded();
        return Collections.unmodifiableCollection(users.values());
    }

    @Override
    public User getUser(String username) {
        checkLoaded();
        return this.users.get(username);
    }

    @Override
    public List<ChatMessage> getAllMessages() {
        checkLoaded();
        return Collections.unmodifiableList(messages);
    }

    public void onMessage(ChatMessage message) {
        this.messages.add(message);
        ((UserImpl) message.getSender()).onMessage(message);
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

    protected abstract void load() throws ConnectionException;

    protected void checkLoaded() throws NotLoadedException {
        if (!isLoaded()) {
            throw new NotLoadedException();
        }
    }
}

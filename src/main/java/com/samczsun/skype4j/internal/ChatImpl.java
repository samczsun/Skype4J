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

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
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
import java.util.logging.Level;

public abstract class ChatImpl implements Chat {
    protected static final String CHAT_INFO_URL = "https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/?view=msnp24Equivalent";
    protected static final String SEND_MESSAGE_URL = "https://%sclient-s.gateway.messenger.live.com/v1/users/ME/conversations/%s/messages";
    protected static final String MODIFY_MEMBER_URL = "https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/members/8:%s";
    protected static final String MODIFY_PROPERTY_URL = "https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/properties?name=%s";
    protected static final String GET_JOIN_URL = "https://api.scheduler.skype.com/threads";
    protected static final String ADD_MEMBER_URL = "https://client-s.gateway.messenger.live.com/v1/threads/%s/members/8:%s";

    protected final AtomicBoolean isLoading = new AtomicBoolean(false);
    protected final AtomicBoolean hasLoaded = new AtomicBoolean(false);

    protected final Map<String, User> users = new ConcurrentHashMap<>();
    protected final List<ChatMessage> messages = new CopyOnWriteArrayList<>();

    private final SkypeImpl client;
    private final String identity;

    ChatImpl(SkypeImpl client, String identity) throws ConnectionException, ChatNotFoundException, IOException {
        this.client = client;
        this.identity = identity;
        load();
    }

    @Override
    public ChatMessage sendMessage(Message message) throws ConnectionException, IOException {
        checkLoaded();
        long ms = System.currentTimeMillis();
        JsonObject obj = new JsonObject();
        obj.add("content", message.write());
        obj.add("messagetype", "RichText");
        obj.add("contenttype", "text");
        obj.add("clientmessageid", String.valueOf(ms));

        ConnectionBuilder builder = new ConnectionBuilder();
        builder.setUrl(client.withCloud(SEND_MESSAGE_URL, getIdentity()));
        builder.setMethod("POST", true);
        builder.addHeader("RegistrationToken", client.getRegistrationToken());
        builder.addHeader("Content-Type", "application/json");
        builder.setData(obj.toString());
        HttpURLConnection con = builder.build();

        if (con.getResponseCode() != 201) {
            throw client.generateException("While sending message", con);
        }

        return ChatMessageImpl.createMessage(this, getUser(client.getUsername()), null, String.valueOf(ms), ms, message);
    }

    @Override
    public Collection<User> getAllUsers() {
        checkLoaded();
        return Collections.unmodifiableCollection(users.values());
    }

    @Override
    public User getUser(String username) {
        checkLoaded();
        return this.users.get(username.toLowerCase());
    }

    @Override
    public List<ChatMessage> getAllMessages() {
        checkLoaded();
        return Collections.unmodifiableList(messages);
    }

    @Override
    public String getIdentity() {
        return this.identity;
    }

    @Override
    public SkypeImpl getClient() {
        return this.client;
    }

    // Begin internal access methods
    public static Chat createChat(Skype client, String identity) throws ConnectionException, ChatNotFoundException, IOException{
        Validate.notNull(client, "Client must not be null");
        Validate.isTrue(client instanceof SkypeImpl, String.format("Now is not the time to use that, %s", client.getUsername()));
        Validate.notEmpty(identity, "Identity must not be null/empty");
        if (identity.startsWith("19:")) {
            if (identity.endsWith("@thread.skype")) {
                return new ChatGroup((SkypeImpl) client, identity);
            } else {
                throw new IllegalArgumentException(String.format("Cannot load P2P chat with identity %s", identity));
            }
        } else if (identity.startsWith("8:")) {
            return new ChatIndividual((SkypeImpl) client, identity);
        } else {
            throw new IllegalArgumentException(String.format("Unknown chat type with identity %s", identity));
        }
    }


    public void onMessage(ChatMessage message) {
        this.messages.add(message);
        ((UserImpl) message.getSender()).onMessage(message);
    }

    public boolean isLoaded() {
        return !isLoading.get() && hasLoaded.get();
    }

    public abstract void addUser(String username) throws ConnectionException, IOException;

    public abstract void removeUser(String username);

    protected abstract void load() throws ConnectionException, ChatNotFoundException, IOException;

    protected void checkLoaded() {
        if (!isLoaded()) {
            throw new NotLoadedException();
        }
    }
}

/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.Visibility;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.events.EventDispatcher;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NoPermissionException;
import com.samczsun.skype4j.internal.chat.ChatImpl;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.ContactRequest;
import org.java_websocket.client.WebSocketClient;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SkypeImpl implements Skype {
    public static final Pattern PAGE_SIZE_PATTERN = Pattern.compile("pageSize=([0-9]+)");
    protected final AtomicBoolean loggedIn = new AtomicBoolean(false);
    protected final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    protected final UUID guid = UUID.randomUUID();
    protected final Set<String> resources;
    protected final String username;

    protected EventDispatcher eventDispatcher = new SkypeEventDispatcher(this);
    protected String skypeToken;
    protected String registrationToken;
    protected String cloud = "";
    protected String endpointId;
    protected Map<String, String> cookies;

    protected Thread sessionKeepaliveThread;
    protected Thread activeThread;
    protected Thread pollThread;
    protected WebSocketClient wss;

    protected String conversationBackwardLink;
    protected String conversationSyncState;

    protected Logger logger = Logger.getLogger(Skype.class.getCanonicalName());
    protected final ExecutorService scheduler = Executors.newFixedThreadPool(4);
    protected final Map<String, Chat> allChats = new ConcurrentHashMap<>();
    protected final Map<String, Contact> allContacts = new ConcurrentHashMap<>();
    protected final List<ContactRequest> allContactRequests = new ArrayList<>();

    public SkypeImpl(String username, Set<String> resources, Logger logger) {
        this.username = username;
        this.resources = Collections.unmodifiableSet(new HashSet<>(resources));
        if (logger != null) {
            this.logger = logger;
        }
    }

    public String getRegistrationToken() {
        return this.registrationToken;
    }

    public String getSkypeToken() {
        return this.skypeToken;
    }

    public String getCloud() {
        return this.cloud;
    }

    public Map<String, String> getCookies() {
        return this.cookies;
    }

    public EventDispatcher getEventDispatcher() {
        return this.eventDispatcher;
    }

    public boolean isShutdownRequested() {
        return this.shutdownRequested.get();
    }

    public Logger getLogger() {
        return this.logger;
    }

    public List<Chat> loadMoreChats(int amount) throws ConnectionException {
        try {
            JsonObject data = null;
            if (this.conversationBackwardLink == null) {
                if (this.conversationSyncState == null) {
                    HttpURLConnection connection = Endpoints.LOAD_CHATS.open(this, System.currentTimeMillis(), amount).get();
                    if (connection.getResponseCode() != 200) {
                        throw ExceptionHandler.generateException("While loading chats", connection);
                    }
                    data = JsonObject.readFrom(new InputStreamReader(connection.getInputStream()));
                } else {
                    return Collections.emptyList();
                }
            } else {
                Matcher matcher = PAGE_SIZE_PATTERN.matcher(this.conversationBackwardLink);
                matcher.find();
                String url = matcher.replaceAll("pageSize=" + amount);
                HttpURLConnection connection = Endpoints.custom(url, this).header("RegistrationToken", this.getRegistrationToken()).get();
                if (connection.getResponseCode() != 200) {
                    throw ExceptionHandler.generateException("While loading chats", connection);
                }
                data = JsonObject.readFrom(new InputStreamReader(connection.getInputStream()));
            }

            List<Chat> chats = new ArrayList<>();

            for (JsonValue value : data.get("conversations").asArray()) {
                try {
                    chats.add(this.getOrLoadChat(value.asObject().get("id").asString()));
                } catch (ChatNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            JsonObject metadata = data.get("_metadata").asObject();
            if (metadata.get("backwardLink") != null) {
                this.conversationBackwardLink = metadata.get("backwardLink").asString();
            } else {
                this.conversationBackwardLink = null;
            }
            this.conversationSyncState = metadata.get("syncState").asString();
            return chats;
        } catch (IOException e) {
            throw ExceptionHandler.generateException("While loading chats", e);
        }
    }

    protected JsonObject buildSubscriptionObject() {
        JsonObject subscriptionObject = new JsonObject();
        subscriptionObject.add("channelType", "httpLongPoll");
        subscriptionObject.add("template", "raw");
        JsonArray interestedResources = new JsonArray();
        for (String s : this.resources) {
            interestedResources.add(s);
        }
        subscriptionObject.add("interestedResources", interestedResources);
        return subscriptionObject;
    }

    protected JsonObject buildRegistrationObject() {
        JsonObject registrationObject = new JsonObject();
        registrationObject.add("id", "messagingService");
        registrationObject.add("type", "EndpointPresenceDoc");
        registrationObject.add("selfLink", "uri");
        JsonObject publicInfo = new JsonObject();
        publicInfo.add("capabilities", "video|audio");
        publicInfo.add("type", 1);
        publicInfo.add("skypeNameVersion", "skype.com");
        publicInfo.add("nodeInfo", "xx");
        publicInfo.add("version", "908/1.16.0.82//skype.com");
        JsonObject privateInfo = new JsonObject();
        privateInfo.add("epname", "Skype4J");
        registrationObject.add("publicInfo", publicInfo);
        registrationObject.add("privateInfo", privateInfo);
        return registrationObject;
    }

    public void shutdown() {
        loggedIn.set(false);
        shutdownRequested.set(true);
        pollThread.interrupt();
        sessionKeepaliveThread.interrupt();
        activeThread.interrupt();
        scheduler.shutdownNow();
        while (!scheduler.isTerminated()) ;
        try {
            scheduler.awaitTermination(1, TimeUnit.DAYS);
            this.wss.closeBlocking();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isLoggedIn() {
        return loggedIn.get();
    }

    public ExecutorService getScheduler() {
        return this.scheduler;
    }

    public String getUsername() {
        return this.username;
    }

    public UUID getGuid() {
        return guid;
    }

    protected void updateCloud(String anyLocation) {
        Pattern grabber = Pattern.compile("https?://([^-]*-)client-s");
        Matcher m = grabber.matcher(anyLocation);
        if (m.find()) {
            this.cloud = m.group(1);
        } else {
            throw new IllegalArgumentException("Could not find match in " + anyLocation);
        }
    }

    @Override
    public Chat getChat(String name) {
        return allChats.get(name);
    }

    @Override
    public Chat loadChat(String name) throws ConnectionException, ChatNotFoundException {
        if (!allChats.containsKey(name)) {
            Chat chat = ChatImpl.createChat(this, name);
            allChats.put(name, chat);
            return chat;
        } else {
            throw new IllegalArgumentException("Chat already exists");
        }
    }

    @Override
    public Chat getOrLoadChat(String name) throws ConnectionException, ChatNotFoundException {
        if (allChats.containsKey(name)) {
            return allChats.get(name);
        } else {
            return loadChat(name);
        }
    }

    @Override
    public Collection<Chat> getAllChats() {
        return Collections.unmodifiableCollection(this.allChats.values());
    }

    @Override
    public GroupChat joinChat(String id) throws ConnectionException, ChatNotFoundException, NoPermissionException {
        Validate.isTrue(id.startsWith("19:") && id.endsWith("@thread.skype"), "Invalid chat id");
        try {
            JsonObject obj = new JsonObject();
            obj.add("role", "User");
            HttpURLConnection connection = Endpoints.ADD_MEMBER_URL.open(this, id, getUsername()).put(obj);
            if (connection.getResponseCode() == 403) {
                throw new NoPermissionException();
            } else if (connection.getResponseCode() == 404) {
                throw new ChatNotFoundException();
            } else if (connection.getResponseCode() != 200) {
                throw ExceptionHandler.generateException("While joining chat", connection);
            }
            return (GroupChat) getOrLoadChat(id);
        } catch (IOException e) {
            throw ExceptionHandler.generateException("While joining chat", e);
        }
    }

    @Override
    public Contact getContact(String name) {
        return this.allContacts.get(name);
    }

    @Override
    public Contact loadContact(String name) throws ConnectionException {
        if (!allContacts.containsKey(name)) {
            Contact contact = ContactImpl.createContact(this, name);
            allContacts.put(name, contact);
            return contact;
        } else {
            throw new IllegalArgumentException("Contact already exists");
        }
    }

    @Override
    public Contact getOrLoadContact(String username) throws ConnectionException {
        if (allContacts.containsKey(username)) {
            return allContacts.get(username);
        } else {
            return loadContact(username);
        }
    }

    @Override
    public Collection<Contact> getAllContacts() {
        return Collections.unmodifiableCollection(this.allContacts.values());
    }

    protected HttpURLConnection registerEndpoint(String skypeToken) throws ConnectionException {
        try {
            HttpURLConnection connection = Endpoints.ENDPOINTS_URL.open(this).header("Authentication", "skypetoken=" + skypeToken).post(new JsonObject()); // LockAndKey data msmsgs@msnmsgr.com:Q1P7W2E4J9R8U3S5
            int code = connection.getResponseCode();
            if (code >= 301 && code <= 303 || code == 307) { //User is in a different cloud - let's go there
                connection = Endpoints.custom(connection.getHeaderField("Location"), this).header("Authentication", "skypetoken=" + skypeToken).post(new JsonObject());
                updateCloud(connection.getHeaderField("Location"));
                code = connection.getResponseCode();
            }
            if (code == 201) {
                return connection;
            } else {
                throw ExceptionHandler.generateException("While registering endpoint", connection);
            }
        } catch (IOException e) {
            throw ExceptionHandler.generateException("While registering endpoint", e);
        }
    }

    protected Connection.Response getAsmToken(Map<String, String> cookies, String skypeToken) throws ConnectionException {
        try {
            return Jsoup.connect(Endpoints.TOKEN_AUTH_URL.url()).cookies(cookies).data("skypetoken", skypeToken).method(Connection.Method.POST).execute();
        } catch (IOException e) {
            throw new ConnectionException("While fetching the asmtoken", e);
        }
    }

    public void setVisibility(Visibility visibility) throws ConnectionException {
        try {
            HttpURLConnection connection = Endpoints.VISIBILITY.open(this).put(new JsonObject().add("status", visibility.internalName()));
            if (connection.getResponseCode() != 200) {
                throw ExceptionHandler.generateException("While setting visibility", connection);
            }
        } catch (IOException e) {
            throw ExceptionHandler.generateException("While setting visibility", e);
        }
    }
}

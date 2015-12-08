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
import com.samczsun.skype4j.internal.threads.ActiveThread;
import com.samczsun.skype4j.internal.threads.PollThread;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.ContactRequest;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SkypeImpl implements Skype {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final Pattern PAGE_SIZE_PATTERN = Pattern.compile("pageSize=([0-9]+)");
    public static final String VERSION = "0.1.5-SNAPSHOT";
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
    protected String endpointIdEncoded;
    protected Map<String, String> cookies;

    protected Thread sessionKeepaliveThread;
    protected Thread activeThread;
    protected PollThread pollThread;
    protected SkypeWebSocket wss;

    protected String conversationBackwardLink;
    protected String conversationSyncState;

    protected Logger logger = Logger.getLogger(Skype.class.getCanonicalName());
    protected final ExecutorService scheduler = Executors.newFixedThreadPool(4, new ThreadFactory() {
        private AtomicInteger id = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Skype4J-Poller-" + username + "-" + id.getAndIncrement());
        }
    });
    protected final ExecutorService shutdownThread;
    protected final Map<String, ChatImpl> allChats = new ConcurrentHashMap<>();
    protected final Map<String, Contact> allContacts = new ConcurrentHashMap<>();
    protected final List<ContactRequest> allContactRequests = new ArrayList<>();

    public SkypeImpl(String username, Set<String> resources, Logger logger) {
        this.username = username;
        this.resources = Collections.unmodifiableSet(new HashSet<>(resources));
        if (logger != null) {
            this.logger = logger;
        } else {
            Handler handler = new ConsoleHandler();
            handler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[").append(record.getLevel().getLocalizedName()).append("] ");
                    sb.append("[").append(new Date(record.getMillis())).append("] ");
                    sb.append(formatMessage(record)).append(LINE_SEPARATOR);

                    if (record.getThrown() != null) {
                        try {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            record.getThrown().printStackTrace(pw);
                            pw.close();
                            sb.append(sw.toString());
                        } catch (Exception ex) {
                        }
                    }
                    return sb.toString();
                }
            });
            this.logger.setUseParentHandlers(false);
            this.logger.addHandler(handler);
        }
        this.shutdownThread = Executors.newSingleThreadExecutor(new SkypeThreadFactory(this, "Shutdown"));
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
                    InputStream input = Endpoints.LOAD_CHATS
                            .open(this, System.currentTimeMillis(), amount)
                            .expect(200, "While loading chats")
                            .get();
                    data = JsonObject.readFrom(new InputStreamReader(input));
                } else {
                    return Collections.emptyList();
                }
            } else {
                Matcher matcher = PAGE_SIZE_PATTERN.matcher(this.conversationBackwardLink);
                matcher.find();
                String url = matcher.replaceAll("pageSize=" + amount);
                data = Endpoints
                        .custom(url, this)
                        .as(JsonObject.class)
                        .expect(200, "While loading chats")
                        .header("RegistrationToken", this.getRegistrationToken())
                        .get();
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
        this.resources.forEach(interestedResources::add);
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
        if (this.loggedIn.get()) {
            loggedIn.set(false);
            shutdownRequested.set(true);
            this.shutdownThread.submit((Runnable) () -> {
                pollThread.shutdown();
                sessionKeepaliveThread.interrupt();
                activeThread.interrupt();
                scheduler.shutdownNow();
                while (!scheduler.isTerminated()) ;
                try {
                    wss.closeBlocking();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                shutdownThread.shutdown();
            });
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
        }
    }

    @Override
    public Chat getChat(String name) {
        return allChats.get(name);
    }

    @Override
    public ChatImpl loadChat(String name) throws ConnectionException, ChatNotFoundException {
        if (!allChats.containsKey(name)) {
            ChatImpl chat = ChatImpl.createChat(this, name);
            allChats.put(name, chat);
            return chat;
        } else {
            throw new IllegalArgumentException("Chat already exists");
        }
    }

    @Override
    public ChatImpl getOrLoadChat(String name) throws ConnectionException, ChatNotFoundException {
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
        JsonObject obj = new JsonObject();
        obj.add("role", "User");
        Endpoints.ADD_MEMBER_URL
                .open(this, id, getUsername())
                .on(403, NoPermissionException::new)
                .on(404, ChatNotFoundException::new)
                .expect(200, "While joining chat")
                .put(obj);
        return (GroupChat) getOrLoadChat(id);
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
        HttpURLConnection connection = Endpoints.ENDPOINTS_URL
                .open(this)
                .as(HttpURLConnection.class)
                .dontConnect()
                .header("Authentication", "skypetoken=" + skypeToken)
                .post(new JsonObject()); // LockAndKey data msmsgs@msnmsgr.com:Q1P7W2E4J9R8U3S5
        try {
            setRegistrationToken(connection.getHeaderField("Set-RegistrationToken"));
            if (connection.getResponseCode() / 100 == 3) {
                updateCloud(connection.getHeaderField("Location"));
            } else if (connection.getResponseCode() != 201) {
                throw ExceptionHandler.generateException("While registering endpoint", connection);
            }
            return connection;
        } catch (IOException e) {
            throw ExceptionHandler.generateException("While registering endpoint", e);
        }
    }

    protected Connection.Response getAsmToken(Map<String, String> cookies, String skypeToken) throws ConnectionException {
        try {
            return Jsoup
                    .connect(Endpoints.TOKEN_AUTH_URL.url())
                    .cookies(cookies)
                    .data("skypetoken", skypeToken)
                    .method(Connection.Method.POST)
                    .execute();
        } catch (IOException e) {
            throw new ConnectionException("While fetching the asmtoken", e);
        }
    }

    public void setVisibility(Visibility visibility) throws ConnectionException {
        Endpoints.VISIBILITY
                .open(this)
                .expect(200, "While updating visibility")
                .put(new JsonObject().add("status", visibility.internalName()));
    }

    public abstract void updateContactList() throws ConnectionException;

    public void registerWebSocket() throws ConnectionException, InterruptedException, URISyntaxException, KeyManagementException, NoSuchAlgorithmException, UnsupportedEncodingException {
        JsonObject trouter = Endpoints.TROUTER_URL
                .open(this)
                .as(JsonObject.class)
                .expect(200, "While fetching trouter data")
                .get();

        JsonObject policyResponse = Endpoints.POLICIES_URL
                .open(this)
                .as(JsonObject.class)
                .expect(200, "While fetching policy data")
                .post(new JsonObject().add("sr", trouter.get("connId")));

        Map<String, String> data = new HashMap<>();
        for (JsonObject.Member value : policyResponse) {
            data.put(value.getName(), value.getValue().asString());
        }
        data.put("r", trouter.get("instance").asString());
        data.put("p", String.valueOf(trouter.get("instancePort").asInt()));
        data.put("ccid", trouter.get("ccid").asString());
        data.put("v", "v2"); //TODO: MAGIC VALUE
        data.put("dom", "web.skype.com"); //TODO: MAGIC VALUE
        data.put("auth", "true"); //TODO: MAGIC VALUE
        data.put("tc", new JsonObject()
                .add("cv", "2015.8.18")
                .add("hr", "")
                .add("v", "1.15.133")
                .toString()); //TODO: MAGIC VALUE
        data.put("timeout", "55");
        data.put("t", String.valueOf(System.currentTimeMillis()));
        StringBuilder args = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            args
                    .append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                    .append("&");
        }

        String socketURL = trouter.get("socketio").asString();
        socketURL = socketURL.substring(socketURL.indexOf('/') + 2);
        socketURL = socketURL.substring(0, socketURL.indexOf(':'));

        String websocketData = Endpoints
                .custom(String.format("%s/socket.io/1/?%s", "https://" + socketURL, args.toString()), this)
                .as(String.class)
                .expect(200, "While fetching websocket details")
                .get();

        Endpoints.REGISTRATIONS
                .open(this)
                .expect(202, "While registering websocket")
                .post(new JsonObject()
                        .add("clientDescription", new JsonObject()
                                .add("aesKey", "")
                                .add("languageId", "en-US")
                                .add("platform", "Chrome")
                                .add("platformUIVersion", "908/1.16.0.82//skype.com")
                                .add("templateKey", "SkypeWeb_1.1"))
                        .add("registrationId", UUID.randomUUID().toString())
                        .add("nodeId", "")
                        .add("transports", new JsonObject().add("TROUTER", new JsonArray().add(new JsonObject()
                                .add("context", "")
                                .add("ttl", 3600)
                                .add("path", trouter.get("surl"))))));

        this.wss = new SkypeWebSocket(this,
                new URI(String.format("%s/socket.io/1/websocket/%s?%s", "wss://" + socketURL,
                        websocketData.split(":")[0], args.toString())));
        this.wss.connectBlocking();
    }

    public void subscribe() throws ConnectionException {
        try {
            HttpURLConnection connection = Endpoints.SUBSCRIPTIONS_URL
                    .open(this)
                    .dontConnect()
                    .post(buildSubscriptionObject());
            if (connection.getResponseCode() == 404) {
                setRegistrationToken(connection.getHeaderField("Set-RegistrationToken"));
                Endpoints.custom("https://" + this.cloud + "client-s.gateway.messenger.live.com/v1/users/ME/endpoints/" + this.endpointIdEncoded, this)
                         .header("RegistrationToken", getRegistrationToken())
                         .expect(200, "Err").put(new JsonObject());
                connection = Endpoints.SUBSCRIPTIONS_URL
                        .open(this)
                        .dontConnect()
                        .post(buildSubscriptionObject());
            }
            if (connection.getResponseCode() != 201) {
                throw ExceptionHandler.generateException("While subscribing", connection);
            }
            Endpoints.MESSAGINGSERVICE_URL
                    .open(this, endpointIdEncoded)
                    .expect(200, "While submitting messagingservice")
                    .put(buildRegistrationObject());
            (pollThread = new PollThread(this, endpointIdEncoded)).start();
            (activeThread = new ActiveThread(this, endpointIdEncoded)).start();
        } catch (IOException io) {
            throw ExceptionHandler.generateException("While subscribing", io);
        } catch (ConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setRegistrationToken(String registrationToken)  {
        String[] splits = registrationToken.split(";");
        String tRegistrationToken = splits[0];
        String tEndpointId = splits[2].split("=")[1];

        this.registrationToken = tRegistrationToken;
        this.endpointId = tEndpointId;
        try {
            this.endpointIdEncoded = URLEncoder.encode(endpointId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public SkypeWebSocket getWebSocket() {
        return wss;
    }
}

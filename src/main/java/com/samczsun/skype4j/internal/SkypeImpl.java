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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.events.EventDispatcher;
import com.samczsun.skype4j.events.chat.DisconnectedEvent;
import com.samczsun.skype4j.events.error.MajorErrorEvent;
import com.samczsun.skype4j.events.error.MinorErrorEvent;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.InvalidCredentialsException;
import com.samczsun.skype4j.exceptions.ParseException;
import com.samczsun.skype4j.user.Contact;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkypeImpl extends Skype {
    private static final String LOGIN_URL = "https://login.skype.com/login?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com";
    private static final String PING_URL = "https://web.skype.com/api/v1/session-ping";
    private static final String TOKEN_AUTH_URL = "https://api.asm.skype.com/v1/skypetokenauth";
    private static final String LOGOUT_URL = "https://login.skype.com/logout?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com&intsrc=client-_-webapp-_-production-_-go-signin";
    private static final String ENDPOINTS_URL = "https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints";
    private static final String THREAD_URL = "https://client-s.gateway.messenger.live.com/v1/threads";
    // The endpoints below all depend on the cloud the user is in
    private static final String SUBSCRIPTIONS_URL = "https://%sclient-s.gateway.messenger.live.com/v1/users/ME/endpoints/SELF/subscriptions";
    private static final String MESSAGINGSERVICE_URL = "https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints/%s/presenceDocs/messagingService";
    private static final String POLL_URL = "https://%sclient-s.gateway.messenger.live.com/v1/users/ME/endpoints/SELF/subscriptions/0/poll";

    private static final Pattern URL_PATTERN = Pattern.compile("threads/(.*)", Pattern.CASE_INSENSITIVE);

    private final AtomicBoolean loggedIn = new AtomicBoolean(false);

    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);

    private final String username;
    private final String password;
    private final Set<String> resources;

    private EventDispatcher eventDispatcher;
    private String skypeToken;
    private String registrationToken;
    private String endpointId;
    private Map<String, String> cookies;

    private String cloud = "";

    private Thread sessionKeepaliveThread;
    private Thread pollThread;

    private final ExecutorService scheduler = Executors.newFixedThreadPool(16);

    private final Map<String, Chat> allChats = new ConcurrentHashMap<>();
    private final Map<String, Contact> allContacts = new ConcurrentHashMap<>();

    private Logger logger = Logger.getLogger(Skype.class.getCanonicalName());

    public SkypeImpl(String username, String password, Set<String> resources, Logger customLogger) {
        this.username = username;
        this.password = password;
        this.resources = resources;
        if (customLogger != null) {
            this.logger = customLogger;
        }
    }

    public void subscribe() throws ConnectionException {
        try {
            ConnectionBuilder builder = new ConnectionBuilder();
            builder.setUrl(withCloud(SUBSCRIPTIONS_URL));
            builder.setMethod("POST", true);
            builder.addHeader("RegistrationToken", registrationToken);
            builder.addHeader("Content-Type", "application/json");
            builder.setData(buildSubscriptionObject().toString());
            HttpURLConnection connection = builder.build();

            int code = connection.getResponseCode();
            if (code != 201) {
                throw generateException("While subscribing", connection);
            }

            builder.setUrl(String.format(MESSAGINGSERVICE_URL, URLEncoder.encode(endpointId, "UTF-8")));
            builder.setMethod("PUT", true);
            builder.setData(buildRegistrationObject().toString());
            connection = builder.build();

            code = connection.getResponseCode();
            if (code != 200) {
                throw generateException("While submitting a messaging service", connection);
            }
            pollThread = new Thread(String.format("Skype-%s-PollThread", username)) {
                public void run() {
                    ConnectionBuilder poll = new ConnectionBuilder();
                    poll.setUrl(withCloud(POLL_URL));
                    poll.setMethod("POST", true);
                    poll.addHeader("RegistrationToken", registrationToken);
                    poll.addHeader("Content-Type", "application/json");
                    poll.setData("");

                    main:
                    while (loggedIn.get()) {
                        try {
                            HttpURLConnection connection = poll.build();
                            AtomicInteger code = new AtomicInteger(0);
                            while (code.get() == 0) {
                                try {
                                    code.set(connection.getResponseCode());
                                } catch (SocketTimeoutException e) {
                                    if (Thread.currentThread().isInterrupted()) {
                                        break main;
                                    }
                                }
                            }

                            if (code.get() != 200) {
                                MajorErrorEvent event = new MajorErrorEvent();
                                getEventDispatcher().callEvent(event);
                                shutdown();
                                break main;
                            }

                            if (scheduler.isShutdown()) {
                                if (!shutdownRequested.get()) {
                                    MajorErrorEvent event = new MajorErrorEvent();
                                    getEventDispatcher().callEvent(event);
                                    shutdown();
                                }
                                break main;
                            }

                            final JsonObject message = JsonObject.readFrom(new InputStreamReader(connection.getInputStream()));
                            scheduler.execute(new Runnable() {
                                public void run() {
                                    if (message.get("eventMessages") != null) {
                                        for (JsonValue elem : message.get("eventMessages").asArray()) {
                                            JsonObject eventObj = elem.asObject();
                                            EventType type = EventType.getByName(eventObj.get("resourceType").asString());
                                            if (type != null) {
                                                try {
                                                    type.handle(SkypeImpl.this, eventObj);
                                                } catch (Throwable t) {
                                                    MinorErrorEvent event = new MinorErrorEvent();
                                                    getEventDispatcher().callEvent(event);
                                                }
                                            } else {
                                                MinorErrorEvent event = new MinorErrorEvent();
                                                getEventDispatcher().callEvent(event);
                                            }
                                        }
                                    }
                                }
                            });
                        } catch (IOException e) {
                            MajorErrorEvent event = new MajorErrorEvent();
                            getEventDispatcher().callEvent(event);
                            shutdown();
                        }
                    }
                }
            };
            pollThread.start();
        } catch (IOException io) {
            throw generateException("While subscribing", io);
        }
    }

    @Override
    public Chat getChat(String name) {
        return allChats.get(name);
    }

    @Override
    public Chat loadChat(String name) throws ConnectionException, ChatNotFoundException, IOException {
        if (!allChats.containsKey(name)) {
            Chat chat = ChatImpl.createChat(this, name);
            allChats.put(name, chat);
            return chat;
        } else {
            throw new IllegalArgumentException("Chat already exists");
        }
    }

    @Override
    public Collection<Chat> getAllChats() {
        return Collections.unmodifiableCollection(this.allChats.values());
    }

    @Override
    public Contact getContact(String name) {
        return this.allContacts.get(name);
    }

    @Override
    public Contact loadContact(String name) throws ConnectionException, IOException {
        if (!allContacts.containsKey(name)) {
            Contact contact = ContactImpl.createContact(this, name);
            allContacts.put(name, contact);
            return contact;
        } else {
            throw new IllegalArgumentException("Contact already exists");
        }
    }

    @Override
    public Contact getOrLoadContact(String username) throws ConnectionException, IOException {
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

    @Override
    public void logout() throws ConnectionException {
        ConnectionBuilder builder = new ConnectionBuilder();
        builder.setUrl(LOGOUT_URL);
        builder.addHeader("Cookies", serializeCookies(cookies));
        try {
            HttpURLConnection con = builder.build();
            if (con.getResponseCode() != 302) {
                throw generateException("While logging out", con);
            }
            shutdown();
        } catch (IOException e) {
            throw generateException("While logging out", e);
        }
    }

    private void shutdown() {
        loggedIn.set(false);
        shutdownRequested.set(true);
        pollThread.interrupt();
        sessionKeepaliveThread.interrupt();
        scheduler.shutdownNow();
        while (!scheduler.isTerminated()) ;
    }

    public String getRegistrationToken() {
        return this.registrationToken;
    }

    public String getSkypeToken() {
        return this.skypeToken;
    }

    @Override
    public EventDispatcher getEventDispatcher() {
        return this.eventDispatcher;
    }

    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public GroupChat createGroupChat(Contact... contacts) throws ConnectionException, ChatNotFoundException {
        try {
            JsonObject obj = new JsonObject();
            JsonArray allContacts = new JsonArray();
            JsonObject me = new JsonObject();
            me.add("id", "8:" + this.getUsername());
            me.add("role", "Admin");
            allContacts.add(me);
            for (Contact contact : contacts) {
                JsonObject other = new JsonObject();
                other.add("id", "8:" + contact.getUsername());
                other.add("role", "User");
                allContacts.add(other);
            }
            obj.add("members", allContacts);
            ConnectionBuilder builder = new ConnectionBuilder();
            builder.setUrl(THREAD_URL);
            builder.setMethod("POST", true);
            builder.addHeader("RegistrationToken", getRegistrationToken());
            builder.setData(obj.toString());
            HttpURLConnection con = builder.build();
            if (con.getResponseCode() != 201) {
                throw generateException("While creating group chat", con);
            }
            String url = con.getHeaderField("Location");
            Matcher chatMatcher = URL_PATTERN.matcher(url);
            if (chatMatcher.find()) {
                GroupChat result = (GroupChat) this.getChat(chatMatcher.group(1));
                while (result == null) {
                    result = (GroupChat) this.getChat(chatMatcher.group(1));
                }
                return result;
            } else {
                throw new IllegalArgumentException("Unable to create chat");
            }
        } catch (IOException e) {
            throw generateException("While creating group chat", e);
        }
    }

    private Response postToLogin(String username, String password) throws ConnectionException {
        try {
            Map<String, String> data = new HashMap<>();
            Document loginDocument = Jsoup.connect(LOGIN_URL).get();
            Element loginForm = loginDocument.getElementById("loginForm");
            for (Element input : loginForm.getElementsByTag("input")) {
                data.put(input.attr("name"), input.attr("value"));
            }
            Date now = new Date();
            data.put("timezone_field", new SimpleDateFormat("XXX").format(now).replace(':', '|'));
            data.put("username", username);
            data.put("password", password);
            data.put("js_time", String.valueOf(now.getTime() / 1000));
            return Jsoup.connect(LOGIN_URL).data(data).method(Method.POST).execute();
        } catch (IOException e) {
            throw new ConnectionException("While submitting credentials", e);
        }
    }

    private Response getAsmToken(Map<String, String> cookies, String skypeToken) throws ConnectionException {
        try {
            return Jsoup.connect(TOKEN_AUTH_URL).cookies(cookies).data("skypetoken", skypeToken).method(Method.POST).execute();
        } catch (IOException e) {
            throw new ConnectionException("While fetching the asmtoken", e);
        }
    }

    private HttpURLConnection registerEndpoint(String skypeToken) throws ConnectionException {
        try {
            ConnectionBuilder builder = new ConnectionBuilder();
            builder.setUrl(ENDPOINTS_URL);
            builder.setMethod("POST", true);
            builder.addHeader("Authentication", String.format("skypetoken=%s", skypeToken));
            builder.setData("{}");

            HttpURLConnection connection = builder.build(); // LockAndKey data msmsgs@msnmsgr.com:Q1P7W2E4J9R8U3S5
            int code = connection.getResponseCode();
            if (code >= 301 && code <= 303 || code == 307) { //User is in a different cloud - let's go there
                builder.setUrl(connection.getHeaderField("Location"));
                updateCloud(connection.getHeaderField("Location"));
                connection = builder.build();
                code = connection.getResponseCode();
            }
            if (code == 201) {
                return connection;
            } else {
                throw generateException("While registering endpoint", connection);
            }
        } catch (IOException e) {
            throw generateException("While registering endpoint", e);
        }
    }

    private JsonObject buildSubscriptionObject() {
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

    private JsonObject buildRegistrationObject() {
        JsonObject registrationObject = new JsonObject();
        registrationObject.add("id", "messagingService");
        registrationObject.add("type", "EndpointPresenceDoc");
        registrationObject.add("selfLink", "uri");
        JsonObject publicInfo = new JsonObject();
        publicInfo.add("capabilities", "video|audio");
        publicInfo.add("type", 1);
        publicInfo.add("skypeNameVersion", "skype.com");
        publicInfo.add("nodeInfo", "xx");
        publicInfo.add("version", "908/1.13.0.79//skype.com");
        JsonObject privateInfo = new JsonObject();
        privateInfo.add("epname", "Skype4J");
        registrationObject.add("publicInfo", publicInfo);
        registrationObject.add("privateInfo", privateInfo);
        return registrationObject;
    }

    public void login() throws InvalidCredentialsException, ConnectionException, ParseException {
        final UUID guid = UUID.randomUUID();
        final Map<String, String> tCookies = new HashMap<>();
        final Response loginResponse = postToLogin(username, password);
        tCookies.putAll(loginResponse.cookies());
        Document loginResponseDocument;
        try {
            loginResponseDocument = loginResponse.parse();
        } catch (IOException e) {
            throw new ParseException("While parsing the login response", e);
        }
        Elements inputs = loginResponseDocument.select("input[name=skypetoken]");
        if (inputs.size() > 0) {
            String tSkypeToken = inputs.get(0).attr("value");

            Response asmResponse = getAsmToken(tCookies, tSkypeToken);
            tCookies.putAll(asmResponse.cookies());

            HttpURLConnection registrationToken = registerEndpoint(tSkypeToken);
            String[] splits = registrationToken.getHeaderField("Set-RegistrationToken").split(";");
            String tRegistrationToken = splits[0];
            String tEndpointId = splits[2].split("=")[1];

            this.skypeToken = tSkypeToken;
            this.registrationToken = tRegistrationToken;
            this.endpointId = tEndpointId;
            this.cookies = tCookies;

            sessionKeepaliveThread = new Thread(String.format("Skype-%s-Session", username)) {
                public void run() {
                    while (loggedIn.get()) {
                        try {
                            Jsoup.connect(PING_URL).header("X-Skypetoken", skypeToken).cookies(cookies).data("sessionId", guid.toString()).post();
                        } catch (IOException e) {
                            eventDispatcher.callEvent(new DisconnectedEvent(e));
                        }
                        try {
                            Thread.sleep(300000);
                        } catch (InterruptedException e) {
                            logger.log(Level.SEVERE, "Session thread was interrupted", e);
                        }
                    }
                }
            };
            sessionKeepaliveThread.start();
            this.eventDispatcher = new SkypeEventDispatcher(this);
            loggedIn.set(true);
        } else {
            Elements elements = loginResponseDocument.select(".message_error");
            if (elements.size() > 0) {
                Element div = elements.get(0);
                if (div.children().size() > 1) {
                    Element span = div.child(1);
                    throw new InvalidCredentialsException(span.text());
                }
            }
            throw new InvalidCredentialsException("Could not find error message. Dumping entire page. \n" + loginResponseDocument.html());
        }
    }

    public ConnectionException generateException(String reason, HttpURLConnection connection) {
        try {
            return new ConnectionException(reason, connection.getResponseCode(), connection.getResponseMessage());
        } catch (IOException e) {
            throw new RuntimeException(String.format("IOException while constructing exception (%s, %s)", reason, connection));
        }
    }

    public ConnectionException generateException(String reason, IOException nested) {
        return new ConnectionException(reason, nested);
    }

    private void updateCloud(String anyLocation) {
        Pattern grabber = Pattern.compile("https?://([^-]*-)client-s");
        Matcher m = grabber.matcher(anyLocation);
        if (m.find()) {
            this.cloud = m.group(1);
        } else {
            throw new IllegalArgumentException("Could not find match in " + anyLocation);
        }
    }

    public String withCloud(String url, Object... extraArgs) {
        Object[] format = new Object[extraArgs.length + 1];
        format[0] = cloud;
        for (int i = 1; i < format.length; i++) {
            format[i] = extraArgs[i - 1].toString();
        }
        return String.format(url, format);
    }

    public String serializeCookies(Map<String, String> cookies) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            result.append(cookie.getKey()).append("=").append(cookie.getValue()).append(";");
        }
        return result.toString();
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public String getCookieString() {
        return serializeCookies(cookies);
    }
}

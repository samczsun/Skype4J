package com.samczsun.skype4j.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.StreamUtils;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.EventDispatcher;
import com.samczsun.skype4j.events.chat.ChatJoinedEvent;
import com.samczsun.skype4j.exceptions.SkypeException;

public class SkypeImpl extends Skype {
    private static final String LOGIN_URL = "https://login.skype.com/login?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com";
    private static final String PING_URL = "https://web.skype.com/api/v1/session-ping";
    private static final String SUBSCRIPTIONS_URL = "https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints/SELF/subscriptions";
    private static final String MESSAGINGSERVICE_URL = "https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints/%s/presenceDocs/messagingService";
    private static final String LOGOUT_URL = "https://login.skype.com/logout?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com&intsrc=client-_-webapp-_-production-_-go-signin";

    private final AtomicBoolean loggedIn = new AtomicBoolean(false);
    private final Map<String, String> cookies = new HashMap<>();
    private final String skypeToken;
    private final String registrationToken;
    private final String endpointId;
    private final String username;

    private final EventDispatcher eventDispatcher;

    private final ExecutorService scheduler = Executors.newFixedThreadPool(16);
    private final Logger logger = Logger.getLogger("webskype");
    private final Map<String, Chat> allChats = new ConcurrentHashMap<>();

    public SkypeImpl(String username, String password) throws SkypeException {
        try {
            this.username = username;
            this.eventDispatcher = new SkypeEventDispatcher();
            final UUID guid = UUID.randomUUID();
            Response loginResponse = postToLogin(username, password);
            cookies.putAll(loginResponse.cookies());
            Document loginResponseDocument = loginResponse.parse();
            Elements inputs = loginResponseDocument.select("input[name=skypetoken]");
            if (inputs.size() > 0) {
                skypeToken = inputs.get(0).attr("value");
                Thread sessionKeepaliveThread = new Thread(String.format("Skype-%s-Session", username)) {
                    public void run() {
                        while (loggedIn.get()) {
                            try {
                                Jsoup.connect(PING_URL).header("X-Skypetoken", skypeToken).cookies(cookies).data("sessionId", guid.toString()).post();
                                Thread.sleep(300000);
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                sessionKeepaliveThread.start();

                Response getAsm = Jsoup.connect("https://api.asm.skype.com/v1/skypetokenauth").cookies(cookies).data("skypetoken", skypeToken).method(Method.POST).execute();
                cookies.putAll(getAsm.cookies());
                HttpsURLConnection getReg = (HttpsURLConnection) new URL("https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints").openConnection();
                getReg.setRequestProperty("Authentication", "skypetoken=" + skypeToken);
                getReg.setRequestMethod("POST");
                getReg.setDoOutput(true);
                getReg.getOutputStream().write("{}".getBytes());
                getReg.getInputStream();
                String[] splits = getReg.getHeaderField("Set-RegistrationToken").split(";");
                registrationToken = splits[0];
                endpointId = splits[2].split("=")[1];

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 14);
                Date now = calendar.getTime();

                String urlToUse = "https://client-s.gateway.messenger.live.com/v1/users/ME/conversations?startTime=" + now.getTime() + "&pageSize=100&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread";
                //        while (true) {
                try {
                    URL url = new URL(urlToUse);
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                    con.setRequestProperty("RegistrationToken", registrationToken);
                    String in = StreamUtils.readFully(con.getInputStream());
                    JsonObject obj = JsonObject.readFrom(in);
                    for (JsonValue elem : obj.get("conversations").asArray()) {
                        try {
                            JsonObject conversation = elem.asObject();
                            String id = conversation.get("id").asString();
                            Chat chat = ChatImpl.createChat(this, id);
                            allChats.put(id, chat);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    //                if (obj.get("_metadata").asJsonObject().has("backwardLink")) {
                    //                    urlToUse = obj.get("_metadata").asJsonObject().get("backwardLink").asString();
                    //                    System.out.println("Backwards");
                    //                } else {
                    //                    break;
                    //                }
                    loggedIn.set(true);
                } catch (Exception e) {
                    throw new SkypeException("An exception occured while fetching chats", e);
                }
                //        }
            } else {
                throw new SkypeException("Login failure");
            }
        } catch (IOException e) {
            throw new SkypeException("An exception occured while logging in", e);
        }
    }

    public void subscribe() throws IOException {
        HttpsURLConnection subscribe = (HttpsURLConnection) new URL(SUBSCRIPTIONS_URL).openConnection();
        subscribe.setRequestMethod("POST");
        subscribe.setDoOutput(true);
        subscribe.setRequestProperty("RegistrationToken", registrationToken);
        subscribe.setRequestProperty("Content-Type", "application/json");
        subscribe.getOutputStream().write(buildSubscriptionObject().toString().getBytes());
        subscribe.getInputStream();

        HttpsURLConnection registerEndpoint = (HttpsURLConnection) new URL(String.format(MESSAGINGSERVICE_URL, URLEncoder.encode(endpointId, "UTF-8"))).openConnection();
        registerEndpoint.setRequestMethod("PUT");
        registerEndpoint.setDoOutput(true);
        registerEndpoint.setRequestProperty("RegistrationToken", registrationToken);
        registerEndpoint.setRequestProperty("Content-Type", "application/json");
        registerEndpoint.getOutputStream().write(buildRegistrationObject().toString().getBytes());
        registerEndpoint.getInputStream();

        Thread pollThread = new Thread() {
            public void run() {
                try {
                    URL url = new URL("https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints/SELF/subscriptions/0/poll");
                    HttpsURLConnection c = null;
                    while (loggedIn.get()) {
                        try {
                            c = (HttpsURLConnection) url.openConnection();
                            c.setRequestMethod("POST");
                            c.setDoOutput(true);
                            c.addRequestProperty("Content-Type", "application/json");
                            c.addRequestProperty("RegistrationToken", registrationToken);
                            c.getOutputStream().write(new byte[0]);
                            InputStream read = c.getInputStream();
                            String json = StreamUtils.readFully(read);
                            if (!json.isEmpty()) {
                                final JsonObject message = JsonObject.readFrom(json);
                                scheduler.execute(new Runnable() {
                                    public void run() {
                                        try {
                                            JsonArray arr = message.get("eventMessages").asArray();
                                            for (JsonValue elem : arr) {
                                                JsonObject eventObj = elem.asObject();
                                                String resourceType = eventObj.get("resourceType").asString();
                                                if (resourceType.equals("NewMessage")) {
                                                    JsonObject resource = eventObj.get("resource").asObject();
                                                    String messageType = resource.get("messagetype").asString();
                                                    MessageType type = MessageType.getByName(messageType);
                                                    type.handle(SkypeImpl.this, resource);
                                                } else if (resourceType.equalsIgnoreCase("EndpointPresence")) {
                                                } else if (resourceType.equalsIgnoreCase("UserPresence")) {
                                                } else if (resourceType.equalsIgnoreCase("ConversationUpdate")) { //Not sure what this does
                                                } else if (resourceType.equalsIgnoreCase("ThreadUpdate")) {
                                                    JsonObject resource = eventObj.get("resource").asObject();
                                                    String chatId = resource.get("id").asString();
                                                    Chat chat = getChat(chatId);
                                                    if (chat == null) {
                                                        chat = ChatImpl.createChat(SkypeImpl.this, chatId);
                                                        allChats.put(chatId, chat);
                                                        ChatJoinedEvent e = new ChatJoinedEvent(chat);
                                                        eventDispatcher.callEvent(e);
                                                    }
                                                } else {
                                                    logger.severe("Unhandled resourceType " + resourceType);
                                                    logger.severe(eventObj.toString());
                                                }
                                            }
                                        } catch (Exception e) {
                                            logger.log(Level.SEVERE, "Exception while handling message", e);
                                            logger.log(Level.SEVERE, message.toString());
                                        }
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        pollThread.start();
    }

    @Override
    public Chat getChat(String name) {
        if (allChats.containsKey(name)) {
            return allChats.get(name);
        } else {
            try {
                Chat chat = ChatImpl.createChat(this, name);
                allChats.put(name, chat);
                return getChat(name);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public Collection<Chat> getAllChats() {
        return Collections.unmodifiableCollection(this.allChats.values());
    }

    @Override
    public void logout() throws IOException {
        Jsoup.connect(LOGOUT_URL).cookies(this.cookies).get();
        loggedIn.set(false);
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

    private Response postToLogin(String username, String password) throws IOException {
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
    }

    private JsonObject buildSubscriptionObject() {
        JsonObject subscriptionObject = new JsonObject();
        subscriptionObject.add("channelType", "httpLongPoll");
        subscriptionObject.add("template", "raw");
        JsonArray interestedResources = new JsonArray();
        interestedResources.add("/v1/users/ME/conversations/ALL/properties");
        interestedResources.add("/v1/users/ME/conversations/ALL/messages");
        interestedResources.add("/v1/users/ME/contacts/ALL");
        interestedResources.add("/v1/threads/ALL");
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
        publicInfo.add("skypeNameVersion", "908/1.6.0.286//skype.com");
        publicInfo.add("nodeInfo", "xx");
        publicInfo.add("version", "908/1.5.116");
        JsonObject privateInfo = new JsonObject();
        privateInfo.add("epname", "Skype4J");
        registrationObject.add("publicInfo", publicInfo);
        registrationObject.add("privateInfo", privateInfo);
        return registrationObject;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

}

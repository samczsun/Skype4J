package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.StreamUtils;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.EventDispatcher;
import com.samczsun.skype4j.events.chat.ChatJoinedEvent;
import com.samczsun.skype4j.events.chat.DisconnectedEvent;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.InvalidCredentialsException;
import com.samczsun.skype4j.exceptions.ParseException;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
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

public class SkypeImpl extends Skype {
    private static final String LOGIN_URL = "https://login.skype.com/login?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com";
    private static final String PING_URL = "https://web.skype.com/api/v1/session-ping";
    private static final String TOKEN_AUTH_URL = "https://api.asm.skype.com/v1/skypetokenauth";
    private static final String SUBSCRIPTIONS_URL = "https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints/SELF/subscriptions";
    private static final String MESSAGINGSERVICE_URL = "https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints/%s/presenceDocs/messagingService";
    private static final String ENDPOINTS_URL = "https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints";
    private static final String LOGOUT_URL = "https://login.skype.com/logout?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com&intsrc=client-_-webapp-_-production-_-go-signin";

    private final AtomicBoolean loggedIn = new AtomicBoolean(false);
    private final String username;
    private final String password;

    private EventDispatcher eventDispatcher;
    private String skypeToken;
    private String registrationToken;
    private String endpointId;
    private Map<String, String> cookies;

    private Thread sessionKeepaliveThread;

    private final ExecutorService scheduler = Executors.newFixedThreadPool(16);
    private final Logger logger = Logger.getLogger("webskype");
    private final Map<String, Chat> allChats = new ConcurrentHashMap<>();

    public SkypeImpl(String username, String password) {
        this.username = username;
        this.password = password;
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
                            eventDispatcher.callEvent(new DisconnectedEvent(e));
                            loggedIn.set(false);
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

    public Logger getLogger() {
        return this.logger;
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

    private HttpsURLConnection registerEndpoint(String skypeToken) throws ConnectionException {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(ENDPOINTS_URL).openConnection(); // msmsgs@msnmsgr.com,Q1P7W2E4J9R8U3S5
            connection.setRequestProperty("Authentication", "skypetoken=" + skypeToken);
            //getReg.setRequestProperty("LockAndKey", "appId=msmsgs@msnmsgr.com; time=1436987361; lockAndKeyResponse=838e6231d460580332d22da83898ff44");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.getOutputStream().write("{}".getBytes());
            connection.getInputStream();
            return connection;
        } catch (IOException e) {
            throw new ConnectionException("While registering the endpoint", e);
        }
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
        publicInfo.add("skypeNameVersion", "skype.com");
        publicInfo.add("nodeInfo", "xx");
        publicInfo.add("version", "908/1.6.0.288//skype.com");
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

            HttpsURLConnection registrationToken = registerEndpoint(tSkypeToken);
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
            this.eventDispatcher = new SkypeEventDispatcher();
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

    @Override
    public String getUsername() {
        return this.username;
    }

}

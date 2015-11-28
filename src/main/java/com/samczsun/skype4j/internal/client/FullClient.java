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

package com.samczsun.skype4j.internal.client;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.events.contact.ContactRequestEvent;
import com.samczsun.skype4j.events.error.MinorErrorEvent;
import com.samczsun.skype4j.events.misc.CaptchaEvent;
import com.samczsun.skype4j.exceptions.CaptchaException;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.InvalidCredentialsException;
import com.samczsun.skype4j.exceptions.ParseException;
import com.samczsun.skype4j.internal.ContactRequestImpl;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.ExceptionHandler;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.SkypeWebSocket;
import com.samczsun.skype4j.internal.StreamUtils;
import com.samczsun.skype4j.internal.Utils;
import com.samczsun.skype4j.internal.threads.ActiveThread;
import com.samczsun.skype4j.internal.threads.KeepaliveThread;
import com.samczsun.skype4j.internal.threads.PollThread;
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
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FullClient extends SkypeImpl {
    private static final Pattern URL_PATTERN = Pattern.compile("threads/(.*)", Pattern.CASE_INSENSITIVE);
    private final String password;

    public FullClient(String username, String password, Set<String> resources, Logger customLogger) {
        super(username, resources, customLogger);
        this.password = password;
    }

    public void subscribe() throws ConnectionException {
        try {
            HttpURLConnection connection = Endpoints.SUBSCRIPTIONS_URL.open(this).post(buildSubscriptionObject());

            int code = connection.getResponseCode();
            if (code != 201) {
                throw ExceptionHandler.generateException("While subscribing", connection);
            }

            connection = Endpoints.MESSAGINGSERVICE_URL.open(this, URLEncoder.encode(endpointId, "UTF-8")).put(buildRegistrationObject());
            code = connection.getResponseCode();
            if (code != 200) {
                throw ExceptionHandler.generateException("While submitting a messaging service", connection);
            }

            connection = Endpoints.AUTH_REQUESTS_URL.open(this).get();
            code = connection.getResponseCode();
            if (code != 200) {
                throw ExceptionHandler.generateException("While fetching contact requests", connection);
            }

            JsonArray contactRequests = JsonArray.readFrom(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            for (JsonValue contactRequest : contactRequests) {
                JsonObject contactRequestObj = contactRequest.asObject();
                try {
                    this.allContactRequests.add(new ContactRequestImpl(contactRequestObj.get("event_time").asString(), getOrLoadContact(contactRequestObj.get("sender").asString()), contactRequestObj.get("greeting").asString(), this));
                } catch (java.text.ParseException e) {
                    getLogger().log(Level.WARNING, "Could not parse date for contact request", e);
                }
            }

            connection = Endpoints.TROUTER_URL.open(this).get();
            code = connection.getResponseCode();
            if (code != 200) {
                throw ExceptionHandler.generateException("While fetching trouter data", connection);
            }

            JsonObject trouter = JsonObject.readFrom(new InputStreamReader(connection.getInputStream(), "UTF-8"));

            JsonObject policyData = new JsonObject();
            policyData.add("sr", trouter.get("connId"));
            connection = Endpoints.POLICIES_URL.open(this).header("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").post(policyData);
            code = connection.getResponseCode();

            if (code != 200) {
                throw ExceptionHandler.generateException("While fetching policy data", connection);
            }

            JsonObject policyResponse = JsonObject.readFrom(new InputStreamReader(connection.getInputStream(), "UTF-8"));

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
            data.put("tc", new JsonObject().add("cv", "2015.8.18").add("hr", "").add("v", "1.15.133").toString()); //TODO: MAGIC VALUE
            data.put("timeout", "55");
            data.put("t", String.valueOf(System.currentTimeMillis()));
            StringBuilder args = new StringBuilder();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                args.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
            }

            String socketURL = trouter.get("socketio").asString();
            socketURL = socketURL.substring(socketURL.indexOf('/') + 2);
            socketURL = socketURL.substring(0, socketURL.indexOf(':'));

            connection = Endpoints.custom(String.format("%s/socket.io/1/?%s", "https://" + socketURL, args.toString()), this).get();
            code = connection.getResponseCode();
            if (code != 200) {
                throw ExceptionHandler.generateException("While fetching websocket details", connection);
            }

            String websocketData = StreamUtils.readFully(connection.getInputStream());
            JsonObject clientDescription = new JsonObject();
            clientDescription.add("aesKey", "");
            clientDescription.add("languageId", "en-US");
            clientDescription.add("platform", "Chrome");
            clientDescription.add("platformUIVersion", "908/1.16.0.82//skype.com");
            clientDescription.add("templateKey", "SkypeWeb_1.1");

            JsonObject trouterObject = new JsonObject();
            trouterObject.add("context", "");
            trouterObject.add("ttl", 3600);
            trouterObject.add("path", trouter.get("surl"));

            JsonArray trouterArray = new JsonArray();
            trouterArray.add(trouterObject);

            JsonObject transports = new JsonObject();
            transports.add("TROUTER", trouterArray);

            JsonObject registrationObject = new JsonObject();
            registrationObject.add("clientDescription", clientDescription);
            registrationObject.add("registrationId", UUID.randomUUID().toString());
            registrationObject.add("nodeId", "");
            registrationObject.add("transports", transports);

            connection = Endpoints.REGISTRATIONS.open(this).post(registrationObject);
            if (connection.getResponseCode() != 202) {
                throw ExceptionHandler.generateException("While registering websocket", connection);
            }

            this.wss = new SkypeWebSocket(this, new URI(String.format("%s/socket.io/1/websocket/%s?%s", "wss://" + socketURL, websocketData.split(":")[0], args.toString())));
            this.wss.connectBlocking();
            (pollThread = new PollThread(this)).start();
            (activeThread = new ActiveThread(this, endpointId)).start();
        } catch (IOException io) {
            throw ExceptionHandler.generateException("While subscribing", io);
        } catch (ConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadAllContacts() throws ConnectionException {
        try {
            HttpURLConnection connection = Endpoints.GET_ALL_CONTACTS.open(this, getUsername()).get();
            if (connection.getResponseCode() != 200) {
                throw ExceptionHandler.generateException("While loading all contacts", connection);
            }
            JsonObject obj = Utils.parseJsonObject(connection.getInputStream());
            for (JsonValue value : obj.get("contacts").asArray()) {
                if (value.asObject().get("suggested") == null || !value.asObject().get("suggested").asBoolean()) {
                    getOrLoadContact(value.asObject().get("id").asString());
                }
            }
        } catch (IOException e) {
            throw ExceptionHandler.generateException("While loading all contacts", e);
        }
    }

    @Override
    public void logout() throws ConnectionException {
        try {
            HttpURLConnection con = Endpoints.LOGOUT_URL.open(this).cookies(cookies).get();
            if (con.getResponseCode() != 302) {
                throw ExceptionHandler.generateException("While logging out", con);
            }
            shutdown();
        } catch (IOException e) {
            throw ExceptionHandler.generateException("While logging out", e);
        }
    }

    public void checkForNewContactRequests() throws ConnectionException, IOException {
        HttpURLConnection connection = Endpoints.AUTH_REQUESTS_URL.open(this).get();
        if (connection.getResponseCode() != 200) {
            throw ExceptionHandler.generateException("While fetching contact requests", connection);
        }

        JsonArray contactRequests = JsonArray.readFrom(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        for (JsonValue contactRequest : contactRequests) {
            JsonObject contactRequestObj = contactRequest.asObject();
            try {
                ContactRequestImpl request = new ContactRequestImpl(contactRequestObj.get("event_time").asString(), getOrLoadContact(contactRequestObj.get("sender").asString()), contactRequestObj.get("greeting").asString(), this);
                if (!this.allContactRequests.contains(request)) {
                    ContactRequestEvent event = new ContactRequestEvent(request);
                    getEventDispatcher().callEvent(event);
                    this.allContactRequests.add(request);
                }
            } catch (java.text.ParseException e) {
                getLogger().log(Level.WARNING, "Could not parse date for contact request", e);
            }
        }
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
            HttpURLConnection con = Endpoints.THREAD_URL.open(this).post(obj);
            if (con.getResponseCode() != 201) {
                throw ExceptionHandler.generateException("While creating group chat", con);
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
            throw ExceptionHandler.generateException("While creating group chat", e);
        }
    }

    private Response postToLogin(String username, String password, String[] captchaData) throws ConnectionException {
        try {
            Map<String, String> data = new HashMap<>();
            Document loginDocument = Jsoup.connect(Endpoints.LOGIN_URL.url()).get();
            Element loginForm = loginDocument.getElementById("loginForm");
            for (Element input : loginForm.getElementsByTag("input")) {
                data.put(input.attr("name"), input.attr("value"));
            }
            Date now = new Date();
            data.put("timezone_field", new SimpleDateFormat("XXX").format(now).replace(':', '|'));
            data.put("username", username);
            data.put("password", password);
            data.put("js_time", String.valueOf(now.getTime() / 1000));
            if (captchaData.length > 0) {
                data.put("hip_solution", captchaData[0]);
                data.put("hip_token", captchaData[1]);
                data.put("fid", captchaData[2]);
                data.put("hip_type", "visual");
                data.put("captcha_provider", "Hip");
            }
            return Jsoup.connect(Endpoints.LOGIN_URL.url()).data(data).method(Method.POST).execute();
        } catch (IOException e) {
            throw new ConnectionException("While submitting credentials", e);
        }
    }

    public void login() throws InvalidCredentialsException, ConnectionException, ParseException {
        login(new String[0]);
    }

    private void login(String[] captchaData) throws InvalidCredentialsException, ConnectionException, ParseException {
        final Map<String, String> tCookies = new HashMap<>();
        final Response loginResponse = postToLogin(username, password, captchaData);
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

            (sessionKeepaliveThread = new KeepaliveThread(this)).start();
            loggedIn.set(true);
        } else {
            boolean foundError = false;
            Elements captchas = loginResponseDocument.select("#captchaContainer");
            if (captchas.size() > 0) {
                Element captcha = captchas.get(0);
                String url = null;
                for (Element scriptTag : captcha.getElementsByTag("script")) {
                    String text = scriptTag.html();
                    if (text.contains("skypeHipUrl")) {
                        url = text.substring(text.indexOf('"') + 1, text.lastIndexOf('"'));
                    }
                }
                if (url != null) {
                    try {
                        HttpURLConnection connection = Endpoints.custom(url, this).get();
                        if (connection.getResponseCode() == 200) {
                            String rawjs = StreamUtils.readFully(connection.getInputStream());
                            Pattern p = Pattern.compile("imageurl:'([^']*)'");
                            Matcher m = p.matcher(rawjs);
                            if (m.find()) {
                                String imgurl = m.group(1);
                                m = Pattern.compile("hid=([^&]*)").matcher(imgurl);
                                if (m.find()) {
                                    String hid = m.group(1);
                                    m = Pattern.compile("fid=([^&]*)").matcher(imgurl);
                                    if (m.find()) {
                                        String fid = m.group(1);
                                        CaptchaEvent event = new CaptchaEvent(imgurl);
                                        getEventDispatcher().callEvent(event);
                                        String response = event.getCaptcha();
                                        if (response != null) {
                                            login(new String[]{response, hid, fid});
                                        } else {
                                            throw new CaptchaException();
                                        }
                                        foundError = true;
                                    }
                                }
                            }
                        } else {
                            MinorErrorEvent err = new MinorErrorEvent(MinorErrorEvent.ErrorSource.PARSING_CAPTCHA, ExceptionHandler.generateException("", connection));
                            getEventDispatcher().callEvent(err);
                        }
                    } catch (IOException e) {
                        MinorErrorEvent err = new MinorErrorEvent(MinorErrorEvent.ErrorSource.PARSING_CAPTCHA, e);
                        getEventDispatcher().callEvent(err);
                    }
                }
            }
            if (!foundError) {
                Elements elements = loginResponseDocument.select(".message_error");
                if (elements.size() > 0) {
                    Element div = elements.get(0);
                    if (div.children().size() > 1) {
                        Element span = div.child(1);
                        throw new InvalidCredentialsException(span.text());
                    }
                } else {
                    throw new InvalidCredentialsException("Could not find error message. Dumping entire page. \n" + loginResponseDocument.html());
                }
            }
        }
    }
}

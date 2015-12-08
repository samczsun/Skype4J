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
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.InvalidCredentialsException;
import com.samczsun.skype4j.exceptions.ParseException;
import com.samczsun.skype4j.internal.ContactImpl;
import com.samczsun.skype4j.internal.ContactRequestImpl;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.ExceptionHandler;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.StreamUtils;
import com.samczsun.skype4j.internal.Utils;
import com.samczsun.skype4j.internal.threads.KeepaliveThread;
import com.samczsun.skype4j.user.Contact;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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

    @Override
    public void loadAllContacts() throws ConnectionException {
        JsonObject object = Endpoints.GET_ALL_CONTACTS
                .open(this, getUsername(), "default")
                .as(JsonObject.class)
                .expect(200, "While loading contacts")
                .get();
        Utils
                .asStream(object.get("contacts").asArray())
                .map(JsonValue::asObject)
                .filter(val -> val.get("suggested") == null || !val.get("suggested").asBoolean())
                .forEach(this::loadContact0);
    }

    private void loadContact0(JsonObject object) {
        if (!this.allContacts.containsKey(object.get("id").asString())) {
            ContactImpl contact = new ContactImpl(this, object);
            this.allContacts.put(object.get("id").asString(), contact);
        }
    }

    @Override
    public void logout() throws ConnectionException {
        Endpoints.LOGOUT_URL.open(this).expect(200, "While logging out").cookies(cookies).get();
        shutdown();
    }

    public void checkForNewContactRequests() throws ConnectionException, IOException {
        JsonArray array = Endpoints.AUTH_REQUESTS_URL
                .open(this)
                .as(JsonArray.class)
                .expect(200, "While loading authorization requests")
                .get();
        for (JsonValue contactRequest : array) {
            JsonObject contactRequestObj = contactRequest.asObject();
            try {
                ContactRequestImpl request = new ContactRequestImpl(contactRequestObj.get("event_time").asString(),
                        getOrLoadContact(contactRequestObj.get("sender").asString()),
                        contactRequestObj.get("greeting").asString(), this);
                if (!this.allContactRequests.contains(request)) {
                    ContactRequestEvent event = new ContactRequestEvent(request);
                    getEventDispatcher().callEvent(event);
                    this.allContactRequests.add(request);
                }
            } catch (java.text.ParseException e) {
                getLogger().log(Level.WARNING, "Could not parse date for contact request", e);
            }
        }
        this.updateContactList();
    }


    private void loadAuthorizationRequests() throws ConnectionException {
        JsonArray array = Endpoints.AUTH_REQUESTS_URL
                .open(this)
                .as(JsonArray.class)
                .expect(200, "While loading authorization requests")
                .get();
        for (JsonValue contactRequest : array) {
            JsonObject contactRequestObj = contactRequest.asObject();
            try {
                this.allContactRequests.add(new ContactRequestImpl(contactRequestObj.get("event_time").asString(),
                        getOrLoadContact(contactRequestObj.get("sender").asString()),
                        contactRequestObj.get("greeting").asString(), this));
            } catch (java.text.ParseException e) {
                getLogger().log(Level.WARNING, "Could not parse date for authorization request", e);
            }
        }
    }

    @Override
    public void updateContactList() throws ConnectionException {
        JsonObject obj = Endpoints.GET_ALL_CONTACTS
                .open(this, getUsername(), "notification")
                .as(JsonObject.class)
                .expect(200, "While loading contacts")
                .get();
        for (JsonValue value : obj.get("contacts").asArray()) {
            if (value.asObject().get("suggested") == null || !value.asObject().get("suggested").asBoolean()) {
                String id = value.asObject().get("id").asString();
                ContactImpl impl = (ContactImpl) allContacts.get(id);
                impl.update(value.asObject());
            }
        }
    }

    @Override
    public GroupChat createGroupChat(Contact... contacts) throws ConnectionException {
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
            HttpURLConnection con = Endpoints.THREAD_URL.open(this).dontConnect().post(obj);
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
            } else {
                data.remove("hip_solution");
                data.remove("hip_token");
                data.remove("fid");
                data.remove("hip_type");
                data.remove("captcha_provider");
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
            this.skypeToken = tSkypeToken;
            this.cookies = tCookies;

            HttpURLConnection registrationToken = registerEndpoint(tSkypeToken);
            setRegistrationToken(registrationToken.getHeaderField("Set-RegistrationToken"));

            this.loadAllContacts();
            this.loadAuthorizationRequests();
            try {
                this.registerWebSocket();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            loggedIn.set(true);
            (sessionKeepaliveThread = new KeepaliveThread(this)).start();
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
                        HttpURLConnection connection = Endpoints.custom(url, this).dontConnect().get();
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
                            MinorErrorEvent err = new MinorErrorEvent(MinorErrorEvent.ErrorSource.PARSING_CAPTCHA,
                                    ExceptionHandler.generateException("", connection));
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
                    throw new InvalidCredentialsException(
                            "Could not find error message. Dumping entire page. \n" + loginResponseDocument.html());
                }
            }
        }
    }
}

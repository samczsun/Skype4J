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
import com.samczsun.skype4j.events.misc.CaptchaEvent;
import com.samczsun.skype4j.exceptions.CaptchaException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.InvalidCredentialsException;
import com.samczsun.skype4j.exceptions.ParseException;
import com.samczsun.skype4j.exceptions.handler.ErrorHandler;
import com.samczsun.skype4j.exceptions.handler.ErrorSource;
import com.samczsun.skype4j.internal.ContactImpl;
import com.samczsun.skype4j.internal.ContactRequestImpl;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.ExceptionHandler;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.threads.AuthenticationChecker;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FullClient extends SkypeImpl {
    private static final Pattern URL_PATTERN = Pattern.compile("threads/(.*)", Pattern.CASE_INSENSITIVE);

    private final String password;

    public FullClient(String username, String password, Set<String> resources, Logger customLogger, List<ErrorHandler> errorHandlers) {
        super(username, resources, customLogger, errorHandlers);
        this.password = password;
    }

    @Override
    public void login() throws InvalidCredentialsException, ConnectionException, ParseException {
        login(new String[0]);
    }

    @Override
    public void logout() throws ConnectionException {
        Endpoints.LOGOUT_URL
                .open(this)
                .noRedirects()
                .expect(code -> (code >= 301 && code <= 303) || code == 307 || code == 308, "While logging out")
                .cookies(cookies)
                .get();
        shutdown();
    }

    @Override
    public void loadAllContacts() throws ConnectionException {
        JsonObject object = Endpoints.GET_ALL_CONTACTS
                .open(this, getUsername(), "default")
                .as(JsonObject.class)
                .expect(200, "While loading contacts")
                .get();
        for (JsonValue value : object.get("contacts").asArray()) {
            JsonObject obj = value.asObject();
            if (obj.get("suggested") == null || !obj.get("suggested").asBoolean()) {
                if (!allContacts.containsKey(obj.get("id").asString())) {
                    this.allContacts.put(obj.get("id").asString(), new ContactImpl(this, obj));
                }
            }
        }
    }

    @Override
    public void getContactRequests(boolean fromWebsocket) throws ConnectionException {
        JsonArray array = Endpoints.AUTH_REQUESTS_URL
                .open(this)
                .as(JsonArray.class)
                .expect(200, "While loading authorization requests")
                .get();
        for (JsonValue contactRequest : array) {
            JsonObject contactRequestObj = contactRequest.asObject();
            try {
                ContactRequestImpl request = new ContactRequestImpl(contactRequestObj.get("event_time").asString(),
                        contactRequestObj.get("sender").asString(),
                        contactRequestObj.get("greeting").asString(), this);
                if (this.allContactRequests.add(request)) {
                    if (fromWebsocket) {
                        ContactRequestEvent event = new ContactRequestEvent(request);
                        getEventDispatcher().callEvent(event);
                    }
                }
            } catch (java.text.ParseException e) {
                getLogger().log(Level.WARNING, "Could not parse date for contact request", e);
            }
        }
        if (fromWebsocket) this.updateContactList();
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
        JsonObject obj = new JsonObject();
        JsonArray allContacts = new JsonArray();
        allContacts.add(new JsonObject().add("id", "8:" + this.getUsername()).add("role", "Admin"));
        for (Contact contact : contacts) {
            allContacts.add(new JsonObject().add("id", "8:" + contact.getUsername()).add("role", "User"));
        }
        obj.add("members", allContacts);
        HttpURLConnection con = Endpoints.THREAD_URL
                .open(this)
                .as(HttpURLConnection.class)
                .expect(201, "While creating group chat")
                .post(obj);
        String url = con.getHeaderField("Location");
        Matcher chatMatcher = URL_PATTERN.matcher(url);
        if (chatMatcher.find()) {
            String id = chatMatcher.group(1);
            while (this.getChat(id) == null) ;
            return (GroupChat) this.getChat(id);
        } else {
            throw ExceptionHandler.generateException("No chat location", con);
        }
    }

    private void login(String[] captchaData) throws InvalidCredentialsException, ConnectionException, ParseException {
        final Response loginResponse = postToLogin(username, password, captchaData);
        this.cookies = new HashMap<>(loginResponse.cookies());
        Document loginResponseDocument;
        try {
            loginResponseDocument = loginResponse.parse();
        } catch (IOException e) {
            throw new ParseException("While parsing the login response", e);
        }
        Elements inputs = loginResponseDocument.select("input[name=skypetoken]");
        if (inputs.size() > 0) {
            this.setSkypeToken(inputs.get(0).attr("value"));
            HttpURLConnection asmResponse = getAsmToken();
            String[] setCookie = asmResponse.getHeaderField("Set-Cookie").split(";")[0].split("=");
            this.cookies.put(setCookie[0], setCookie[1]);

            registerEndpoint();

            this.loadAllContacts();
            this.getContactRequests(false);
            try {
                this.registerWebSocket();
            } catch (Exception e) {
                handleError(ErrorSource.REGISTERING_WEBSOCKET, e, false);
            }
            loggedIn.set(true);
            (sessionKeepaliveThread = new KeepaliveThread(this)).start();
            (reauthThread = new AuthenticationChecker(this)).start();
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
                        String rawjs = Endpoints.custom(url, this).as(String.class).get();
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
                    } catch (ConnectionException e) {
                        handleError(ErrorSource.PARSING_CAPTCHA, e, true);
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
            throw ExceptionHandler.generateException("While submitting credentials", e);
        }
    }
}

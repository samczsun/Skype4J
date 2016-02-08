/*
 * Copyright 2016 Sam Sun <me@samczsun.com>
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
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.InvalidCredentialsException;
import com.samczsun.skype4j.exceptions.handler.ErrorHandler;
import com.samczsun.skype4j.exceptions.handler.ErrorSource;
import com.samczsun.skype4j.internal.*;
import com.samczsun.skype4j.internal.threads.AuthenticationChecker;
import com.samczsun.skype4j.internal.threads.KeepaliveThread;
import com.samczsun.skype4j.internal.utils.Encoder;
import com.samczsun.skype4j.user.Contact;

import javax.xml.bind.DatatypeConverter;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public void login() throws InvalidCredentialsException, ConnectionException {
        Map<String, String> data = new HashMap<>();
        data.put("scopes", "client");
        data.put("clientVersion", "0/7.4.85.102/259/");
        data.put("username", username);
        data.put("passwordHash", hash());
        JsonObject loginData = Endpoints.LOGIN_URL.open(this)
                .as(JsonObject.class)
                .expect(200, "While logging in")
                .post(Encoder.encode(data));
        this.setSkypeToken(loginData.get("skypetoken").asString());
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

    private String hash() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] encodedMD = messageDigest.digest(String.format("%s\nskyper\n%s", username, password).getBytes());
            return DatatypeConverter.printBase64Binary(encodedMD);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

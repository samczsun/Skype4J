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
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.InvalidCredentialsException;
import com.samczsun.skype4j.exceptions.NotParticipatingException;
import com.samczsun.skype4j.exceptions.ParseException;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.ExceptionHandler;
import com.samczsun.skype4j.internal.SkypeEventDispatcher;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.SkypeWebSocket;
import com.samczsun.skype4j.internal.StreamUtils;
import com.samczsun.skype4j.internal.threads.ActiveThread;
import com.samczsun.skype4j.internal.threads.KeepaliveThread;
import com.samczsun.skype4j.internal.threads.PollThread;
import com.samczsun.skype4j.user.Contact;
import org.jsoup.Connection;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class GuestClient extends SkypeImpl {
    private final String chatId;

    public GuestClient(String username, String chatId, Set<String> resources, Logger logger) {
        super(username, resources, logger);
        this.chatId = chatId;
    }

    @Override
    public void login() throws InvalidCredentialsException, ConnectionException, ParseException, NotParticipatingException {
        try {
            JsonObject data = new JsonObject();
            data.add("name", username);
            data.add("threadId", chatId);
            data.add("spaceId", "Skype4J");
            data.add("flowId", "Skype4J");
            HttpURLConnection connection = Endpoints.NEW_GUEST.open(this).header("csrf_token", "skype4j").header("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36").cookie("csrf_token", "skype4j").post(data);
            if (connection.getResponseCode() == 303) {
                throw new NotParticipatingException();
            } else if (connection.getResponseCode() != 201) {
                throw ExceptionHandler.generateException("While loggin in", connection);
            }
            JsonObject response = JsonObject.readFrom(new InputStreamReader(connection.getInputStream()));
            this.skypeToken = response.get("skypetoken").asString();

            Map<String, String> tCookies = new HashMap<>();
            Connection.Response asmResponse = getAsmToken(tCookies, skypeToken);
            tCookies.putAll(asmResponse.cookies());

            HttpURLConnection registrationToken = registerEndpoint(skypeToken);
            String[] splits = registrationToken.getHeaderField("Set-RegistrationToken").split(";");
            String tRegistrationToken = splits[0];
            String tEndpointId = splits[2].split("=")[1];

            this.registrationToken = tRegistrationToken;
            this.endpointId = tEndpointId;
            this.cookies = tCookies;

            (sessionKeepaliveThread = new KeepaliveThread(this)).start();
            this.loggedIn.set(true);
        } catch (IOException e) {
            throw ExceptionHandler.generateException("While loggin in", e);
        }
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
    public void logout() throws ConnectionException {
        try {
            HttpURLConnection connection = Endpoints.LEAVE_GUEST.open(this, this.chatId).header("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36").cookie("guest_spaceId_" + URLEncoder.encode(this.chatId, "UTF-8"), "Skype4J").cookie("guest_token_Skype4J", this.skypeToken).get();
            if (connection.getResponseCode() != 302) {
                throw ExceptionHandler.generateException("While logging out", connection);
            }
            shutdown();
        } catch (IOException e) {
            throw ExceptionHandler.generateException("While logging out", e);
        }
    }

    @Override
    public GroupChat createGroupChat(Contact... contacts) throws ConnectionException, ChatNotFoundException {
        throw new UnsupportedOperationException("Not supported with a guest account");
    }

    @Override
    public void loadAllContacts() throws ConnectionException {
        throw new UnsupportedOperationException("Not supported with a guest account");
    }

    @Override
    public void updateContactList() {
        throw new UnsupportedOperationException("Not supported with a guest account");
    }
}

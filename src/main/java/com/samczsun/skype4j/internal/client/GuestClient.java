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

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.InvalidCredentialsException;
import com.samczsun.skype4j.exceptions.NotParticipatingException;
import com.samczsun.skype4j.exceptions.handler.ErrorHandler;
import com.samczsun.skype4j.exceptions.handler.ErrorSource;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.threads.AuthenticationChecker;
import com.samczsun.skype4j.internal.threads.ServerPingThread;
import com.samczsun.skype4j.internal.utils.UncheckedRunnable;
import com.samczsun.skype4j.user.Contact;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class GuestClient extends SkypeImpl {
    private final String chatId;

    public GuestClient(String username, String chatId, Set<String> resources, Logger logger, List<ErrorHandler> errorHandlers) {
        super(username, resources, logger, errorHandlers);
        this.chatId = chatId;
    }

    @Override
    public void login() throws ConnectionException, InvalidCredentialsException {
        JsonObject response = Endpoints.NEW_GUEST
                .open(this)
                .as(JsonObject.class)
                .on(303, connection -> {
                    throw new NotParticipatingException();
                })
                .expect(201, "While logging in")
                .header("csrf_token", "skype4j")
                .cookie("csrf_token", "skype4j")
                .post(new JsonObject()
                        .add("name", username)
                        .add("threadId", chatId)
                        .add("shortId", "Skype4J")
                        .add("flowId", "Skype4J"));
        this.setSkypeToken(response.get("skypetoken").asString());

        List<UncheckedRunnable> tasks = new ArrayList<>();
        tasks.add(() -> {
            HttpURLConnection asmResponse = getAsmToken();
            String[] setCookie = asmResponse.getHeaderField("Set-Cookie").split(";")[0].split("=");
            this.cookies.put(setCookie[0], setCookie[1]);
        });
        tasks.add(() -> {
            try {
                this.registerWebSocket();
            } catch (Exception e) {
                handleError(ErrorSource.REGISTERING_WEBSOCKET, e, false);
            }
        });
        tasks.add(this::registerEndpoint);

        try {
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            tasks.forEach(executorService::submit);
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        super.login();
    }

    @Override
    public void logout() throws ConnectionException {
        Endpoints.LEAVE_GUEST
                .open(this, this.chatId)
                .expect(200, "While logging out")
                .cookie("guest_token_" + this.chatId, "skype4j::" + this.getSkypeToken())
                .cookie("csrf_token", "skype4j")
                .cookie("launcher_session_id", "Skype4J")
                .get();
        shutdown();
    }

    @Override
    public GroupChat createGroupChat(Contact... contacts) {
        throw new UnsupportedOperationException("Not supported with a guest account");
    }

    @Override
    public void loadAllContacts() {
        throw new UnsupportedOperationException("Not supported with a guest account");
    }

    @Override
    public void getContactRequests(boolean fromWebsocket) {
        throw new UnsupportedOperationException("Not supported with a guest account");
    }

    @Override
    public void updateContactList() {
        throw new UnsupportedOperationException("Not supported with a guest account");
    }
}

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

package com.samczsun.skype4j.internal.threads;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.handler.ErrorSource;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.EventType;
import com.samczsun.skype4j.internal.ExceptionHandler;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.SkypeThreadFactory;
import com.samczsun.skype4j.internal.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PollThread extends Thread {
    private final SkypeImpl skype;
    private final ExecutorService inputFetcher;
    private final String endpointId;
    private final Object lock = new Object();

    private IOException pendingException;
    private HttpURLConnection connection;

    public PollThread(SkypeImpl skype, String endpointId) {
        super(String.format("Skype4J-Poller-%s", skype.getUsername()));
        this.skype = skype;
        this.inputFetcher = Executors.newSingleThreadExecutor(new SkypeThreadFactory(skype, "PollBlocker"));
        this.endpointId = endpointId;
    }

    public void run() {
        int pollId = 0;
        while (skype.isAuthenticated()) {
            final Endpoints.EndpointConnection<HttpURLConnection> epconn = Endpoints.POLL
                    .open(skype, pollId)
                    .header("Content-Type", "application/json")
                    .dontConnect();
            final AtomicBoolean complete = new AtomicBoolean(false);
            while (skype.isAuthenticated()) {
                try {
                    complete.set(false);
                    connection = epconn.post();
                    inputFetcher.execute(() -> {
                        try {
                            connection.getResponseCode();
                        } catch (IOException e) {
                            pendingException = e;
                        } finally {
                            complete.set(true);
                            synchronized (lock) {
                                lock.notify();
                            }
                        }
                    });

                    synchronized (lock) {
                        if (!complete.get()) {
                            lock.wait();
                        }
                    }

                    if (pendingException != null) {
                        skype.handleError(ErrorSource.POLLING_SKYPE, pendingException, false);
                        continue;
                    }

                    if (connection.getHeaderField("Set-RegistrationToken") != null) {
                        skype.setRegistrationToken(connection.getHeaderField("Set-RegistrationToken"));
                    }

                    if (connection.getResponseCode() == 403) {
                        try {
                            HttpURLConnection conn = Endpoints
                                    .custom("https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints/" + endpointId,
                                            skype)
                                    .dontConnect()
                                    .header("Authentication", "skypetoken=" + skype.getSkypeToken())
                                    .put(new JsonObject());
                            if (conn.getResponseCode() != 200) {
                                skype.handleError(ErrorSource.REFRESHING_ENDPOINT,
                                        ExceptionHandler.generateException("While refreshing endpoint", conn), true);
                                return;
                            }
                            String regtoken = conn.getHeaderField("Set-RegistrationToken");
                            if (regtoken != null) {
                                skype.setRegistrationToken(regtoken);
                            }
                            JsonObject object = Utils.parseJsonObject(conn.getInputStream());
                            if (object.get("subscriptions") != null) {
                                pollId = object.get("subscriptions").asArray().get(0).asObject().get("id").asInt();
                            }
                            break;
                        } catch (IOException e) {
                            skype.handleError(ErrorSource.REFRESHING_ENDPOINT, e, true);
                            return;
                        }
                    }

                    if (connection.getResponseCode() != 200) {
                        continue;
                    }

                    if (skype.getScheduler().isShutdown()) {
                        if (!skype.isShutdownRequested()) {
                            skype.handleError(ErrorSource.THREAD_POOL_DEAD, null, true);
                        }
                        return;
                    }

                    final JsonObject message = Utils.parseJsonObject(connection.getInputStream());
                    skype.getScheduler().execute(() -> {
                        if (message.get("eventMessages") != null) {
                            for (JsonValue elem : message.get("eventMessages").asArray()) {
                                JsonObject eventObj = elem.asObject();
                                EventType type = EventType.getByName(eventObj.get("resourceType").asString());
                                if (type != null) {
                                    try {
                                        type.handle(skype, eventObj);
                                    } catch (Throwable t) {
                                        skype.handleError(ErrorSource.PARSING_MESSAGE, t, false);
                                    }
                                } else {
                                    skype.handleError(ErrorSource.NO_MESSAGE_TYPE, null, false);
                                }
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    return;
                } catch (IOException | ConnectionException e) {
                    skype.handleError(ErrorSource.POLLING_SKYPE, e, true);
                    return;
                } finally {
                    connection.disconnect();
                }
            }
        }
    }

    public void shutdown() {
        this.interrupt();
        while (this.getState() != State.TERMINATED) ;
        if (this.connection != null) {
            this.connection.disconnect();
        }
        this.inputFetcher.shutdownNow();
        while (!this.inputFetcher.isTerminated()) ;
    }
}

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
import com.samczsun.skype4j.events.error.MajorErrorEvent;
import com.samczsun.skype4j.events.error.MinorErrorEvent;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.EventType;
import com.samczsun.skype4j.internal.ExceptionHandler;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.SkypeThreadFactory;
import com.samczsun.skype4j.internal.Utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PollThread extends Thread {
    private final SkypeImpl skype;
    private final ExecutorService inputFetcher;

    public PollThread(SkypeImpl skype) {
        super(String.format("Skype4J-Poller-%s", skype.getUsername()));
        this.skype = skype;
        this.inputFetcher = Executors.newSingleThreadExecutor(new SkypeThreadFactory(skype, "PollBlocker"));
    }

    public void run() {
        final Endpoints.EndpointConnection epconn = Endpoints.POLL.open(skype).header("Content-Type", "application/json");
        final AtomicReference<IOException> pendingException = new AtomicReference<>();
        final AtomicReference<HttpURLConnection> connection = new AtomicReference<>();
        final Object lock = new Object();
        while (skype.isLoggedIn()) {
            try {
                connection.set(epconn.post());
                inputFetcher.execute(new Runnable() {
                    public void run() {
                        try {
                            connection.get().getResponseCode();
                        } catch (IOException e) {
                            pendingException.set(e);
                        } finally {
                            synchronized (lock) {
                                lock.notify();
                            }
                        }
                    }
                });

                synchronized (lock) {
                    lock.wait();
                }

                if (pendingException.get() != null) {
                    throw pendingException.get();
                }

                if (connection.get().getResponseCode() != 200) {
                    MajorErrorEvent event = new MajorErrorEvent(MajorErrorEvent.ErrorSource.POLLING_SKYPE, ExceptionHandler.generateException("While polling Skype", connection.get()));
                    skype.getEventDispatcher().callEvent(event);
                    skype.shutdown();
                    return;
                }

                if (skype.getScheduler().isShutdown()) {
                    if (!skype.isShutdownRequested()) {
                        MajorErrorEvent event = new MajorErrorEvent(MajorErrorEvent.ErrorSource.THREAD_POOL_DEAD);
                        skype.getEventDispatcher().callEvent(event);
                        skype.shutdown();
                        return;
                    }
                }

                final JsonObject message = Utils.parseJsonObject(connection.get().getInputStream());
                skype.getScheduler().execute(new Runnable() {
                    public void run() {
                        if (message.get("eventMessages") != null) {
                            for (JsonValue elem : message.get("eventMessages").asArray()) {
                                JsonObject eventObj = elem.asObject();
                                EventType type = EventType.getByName(eventObj.get("resourceType").asString());
                                if (type != null) {
                                    try {
                                        type.handle(skype, eventObj);
                                    } catch (Throwable t) {
                                        MinorErrorEvent event = new MinorErrorEvent(MinorErrorEvent.ErrorSource.PARSING_MESSAGE, t, elem.toString());
                                        skype.getEventDispatcher().callEvent(event);
                                    }
                                } else {
                                    MinorErrorEvent event = new MinorErrorEvent(MinorErrorEvent.ErrorSource.NO_MESSAGE_TYPE, null, elem.toString());
                                    skype.getEventDispatcher().callEvent(event);
                                }
                            }
                        }
                    }
                });
            } catch (InterruptedException e) {
                return;
            } catch (IOException e) {
                MajorErrorEvent event = new MajorErrorEvent(MajorErrorEvent.ErrorSource.POLLING_SKYPE, e);
                skype.getEventDispatcher().callEvent(event);
                skype.shutdown();
                return;
            } finally {
                if (connection.get() != null) {
                    connection.get().disconnect();
                }
            }
        }
    }

    public void shutdown() {
        this.interrupt();
        this.inputFetcher.shutdown();
        while (!this.inputFetcher.isTerminated()) ;
    }
}

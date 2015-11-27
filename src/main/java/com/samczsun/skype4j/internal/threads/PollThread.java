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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PollThread extends Thread {
    private final Object lock = new Object();
    private SkypeImpl skype;
    private ExecutorService inputFetcher = Executors.newSingleThreadExecutor(new ThreadFactory() {
        private AtomicInteger id = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Skype4J-PollBlocker-" + skype.getUsername() + "-" + id.getAndIncrement());
        }
    });

    public PollThread(SkypeImpl skype) {
        super(String.format("Skype4J-Poller-%s", skype.getUsername()));
        this.skype = skype;
    }

    public void run() {
        final Endpoints.EndpointConnection epconn = Endpoints.POLL.open(skype).header("Content-Type", "application/json");
        final AtomicInteger responseCode = new AtomicInteger(0);
        final AtomicReference<IOException> pendingException = new AtomicReference<>();
        main:
        while (skype.isLoggedIn()) {
            try {
                final HttpURLConnection connection = epconn.post();
                inputFetcher.execute(new Runnable() {
                    public void run() {
                        try {
                            responseCode.set(connection.getResponseCode());
                        } catch (IOException e) {
                            pendingException.set(e);
                        } finally {
                            synchronized (lock) {
                                lock.notify();
                            }
                        }
                    }
                });

                try {
                    synchronized (lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    break main;
                }

                if (pendingException.get() != null) {
                    throw pendingException.get();
                }

                if (responseCode.get() != 200) {
                    MajorErrorEvent event = new MajorErrorEvent(MajorErrorEvent.ErrorSource.POLLING_SKYPE, ExceptionHandler.generateException("While polling Skype", connection));
                    skype.getEventDispatcher().callEvent(event);
                    skype.shutdown();
                    break main;
                }

                if (skype.getScheduler().isShutdown()) {
                    if (!skype.isShutdownRequested()) {
                        MajorErrorEvent event = new MajorErrorEvent(MajorErrorEvent.ErrorSource.THREAD_POOL_DEAD);
                        skype.getEventDispatcher().callEvent(event);
                        skype.shutdown();
                    }
                }

                final JsonObject message = JsonObject.readFrom(new InputStreamReader(connection.getInputStream(), "UTF-8"));
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
                                        MinorErrorEvent event = new MinorErrorEvent(MinorErrorEvent.ErrorSource.PARSING_MESSAGE, t);
                                        skype.getEventDispatcher().callEvent(event);
                                    }
                                } else {
                                    MinorErrorEvent event = new MinorErrorEvent(MinorErrorEvent.ErrorSource.NO_MESSAGE_TYPE);
                                    skype.getEventDispatcher().callEvent(event);
                                }
                            }
                        }
                    }
                });
            } catch (IOException e) {
                MajorErrorEvent event = new MajorErrorEvent(MajorErrorEvent.ErrorSource.POLLING_SKYPE, e);
                skype.getEventDispatcher().callEvent(event);
                skype.shutdown();
            }
        }
    }

    public void shutdown() {
        this.interrupt();
        this.inputFetcher.shutdown();
        while (!this.inputFetcher.isTerminated()) ;
    }
}

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

package com.samczsun.skype4j.internal.threads;

import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.handler.ErrorSource;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.SkypeImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerPingThread extends Thread {
    private static final Map<String, AtomicInteger> ID = new ConcurrentHashMap<>();


    private SkypeImpl skype;
    private AtomicBoolean stop = new AtomicBoolean(false);

    public ServerPingThread(SkypeImpl skype) {
        super(String.format("Skype4J-ServerPing-%s-%s", skype.getUsername(), ID.computeIfAbsent(skype.getUsername(), str -> new AtomicInteger()).getAndIncrement()));
        this.skype = skype;
    }

    public void run() {
        while (skype.isLoggedIn() && !stop.get()) {
            if (skype.isAuthenticated() && !stop.get()) {
                try {
                    Endpoints.PING_URL
                            .open(skype)
                            .expect(200, "While maintaining session")
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .cookies(skype.getCookies())
                            .connect("POST", "sessionId=" + skype.getGuid().toString());
                } catch (ConnectionException e) {
                    skype.handleError(ErrorSource.SERVER_PING, e, false); // After reviewing source this appears correct
                }
                if (stop.get()) {
                    return;
                }
                try {
                    Thread.sleep(300000);
                } catch (InterruptedException ignored) {
                }
            } else {
                return;
            }
        }
    }

    public void kill() {
        this.stop.set(true);
        this.interrupt();
    }
}

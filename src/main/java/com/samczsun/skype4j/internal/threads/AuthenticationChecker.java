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

import com.samczsun.skype4j.exceptions.handler.ErrorSource;
import com.samczsun.skype4j.internal.SkypeImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AuthenticationChecker extends Thread {
    private static final Map<String, AtomicInteger> ID = new ConcurrentHashMap<>();

    private SkypeImpl skype;
    private AtomicBoolean stop = new AtomicBoolean(false);

    public AuthenticationChecker(SkypeImpl skype) {
        super(String.format("Skype4J-AuthenticationChecker-%s-%s", skype.getUsername(), ID.computeIfAbsent(skype.getUsername(), str -> new AtomicInteger()).getAndIncrement()));
        this.skype = skype;
    }

    public void run() {
        while (skype.isLoggedIn() && !stop.get()) {
            long diff = (skype.getExpirationTime() - System.currentTimeMillis());
            if (diff > 1800000) { //30 min
                if (stop.get()) {
                    return;
                }
                try {
                    Thread.sleep(diff / 2);
                } catch (InterruptedException ignored) {
                }
            } else {
                if (stop.get()) {
                    return;
                }
                try {
                    skype.reauthenticate();
                } catch (Exception e) {
                    skype.handleError(ErrorSource.REAUTHENTICATING, e, true);
                    //Don't see why you need to return in a finally block.
                } finally {
                    return;
                }
            }
        }
    }

    public void kill() {
        this.stop.set(true);
        this.interrupt();
    }
}

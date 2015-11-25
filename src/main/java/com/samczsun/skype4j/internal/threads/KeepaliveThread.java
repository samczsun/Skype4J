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

import com.samczsun.skype4j.events.error.MajorErrorEvent;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.ExceptionHandler;
import com.samczsun.skype4j.internal.SkypeImpl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;

public class KeepaliveThread extends Thread {
    private SkypeImpl skype;

    public KeepaliveThread(SkypeImpl skype) {
        super(String.format("Skype-%s-session", skype.getUsername()));
        this.skype = skype;
    }

    public void run() {
        while (skype.isLoggedIn()) {
            try {
                HttpURLConnection connection = Endpoints.PING_URL.open(skype).cookies(skype.getCookies()).connect("POST", "sessionId=" + skype.getGuid().toString());
                if (connection.getResponseCode() != 200) {
                    MajorErrorEvent event = new MajorErrorEvent(MajorErrorEvent.ErrorSource.SESSION_KEEPALIVE, ExceptionHandler.generateException("While maintaining session", connection));
                    skype.getEventDispatcher().callEvent(event);
                }
            } catch (IOException e) {
                MajorErrorEvent event = new MajorErrorEvent(MajorErrorEvent.ErrorSource.SESSION_KEEPALIVE, e);
                skype.getEventDispatcher().callEvent(event);
                skype.shutdown();
            }
            try {
                Thread.sleep(300000);
            } catch (InterruptedException e) {
                skype.getLogger().log(Level.SEVERE, "Session thread was interrupted", e);
            }
        }
    }
}

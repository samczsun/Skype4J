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
import com.samczsun.skype4j.events.error.MajorErrorEvent;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.ExceptionHandler;
import com.samczsun.skype4j.internal.SkypeImpl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.logging.Level;

public class ActiveThread extends Thread {

    private SkypeImpl skype;
    private String endpoint;

    public ActiveThread(SkypeImpl skype, String endpoint) {
        super(String.format("Skype4J-Active-%s", skype.getUsername()));
        this.skype = skype;
        this.endpoint = endpoint;
    }

    public void run() {
        while (!skype.isLoggedIn()) {
            try {
                HttpURLConnection connection = Endpoints.ACTIVE.open(skype, URLEncoder.encode(endpoint, "UTF-8")).post(new JsonObject().add("timeout", 12));
                if (connection.getResponseCode() != 201) {
                    MajorErrorEvent event = new MajorErrorEvent(MajorErrorEvent.ErrorSource.SESSION_ACTIVE, ExceptionHandler.generateException("While submitting keepalive", connection));
                    skype.getEventDispatcher().callEvent(event);
                    skype.shutdown();
                }
            } catch (IOException e) {
                MajorErrorEvent event = new MajorErrorEvent(MajorErrorEvent.ErrorSource.SESSION_ACTIVE, e);
                skype.getEventDispatcher().callEvent(event);
                skype.shutdown();
            }
            try {
                Thread.sleep(12000);
            } catch (InterruptedException e) {
                skype.getLogger().log(Level.SEVERE, "Active thread was interrupted", e);
            }
        }
    }
}

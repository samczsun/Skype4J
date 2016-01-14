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
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.handler.ErrorSource;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.SkypeImpl;

public class ActiveThread extends Thread {

    private SkypeImpl skype;
    private String endpoint;

    public ActiveThread(SkypeImpl skype, String endpoint) {
        super(String.format("Skype4J-Active-%s", skype.getUsername()));
        this.skype = skype;
        this.endpoint = endpoint;
    }

    public void run() {
        while (skype.isLoggedIn()) {
            if (skype.isAuthenticated()) {
                try {
                    Endpoints.ACTIVE
                            .open(skype, endpoint)
                            .expect(201, "While submitting active")
                            .post(new JsonObject().add("timeout", 12));
                } catch (ConnectionException e) {
                    skype.handleError(ErrorSource.SESSION_ACTIVE, e, false);
                }
                try {
                    Thread.sleep(12000);
                } catch (InterruptedException e) {
                }
            } else {
                return;
            }
        }
    }
}

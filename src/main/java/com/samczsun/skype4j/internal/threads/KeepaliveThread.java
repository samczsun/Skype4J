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

import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.handler.ErrorSource;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.SkypeImpl;

public class KeepaliveThread extends Thread {
    private SkypeImpl skype;

    public KeepaliveThread(SkypeImpl skype) {
        super(String.format("Skype4J-Keepalive-%s", skype.getUsername()));
        this.skype = skype;
    }

    public void run() {
        while (skype.isLoggedIn()) {
            if (skype.isAuthenticated()) {
                try {
                    Endpoints.PING_URL
                            .open(skype)
                            .expect(200, "While maintaining session")
                            .cookies(skype.getCookies())
                            .connect("POST", "sessionId=" + skype.getGuid().toString());
                } catch (ConnectionException e) {
                    skype.handleError(ErrorSource.SESSION_KEEPALIVE, e, true);
                    return;
                }
                try {
                    Thread.sleep(300000);
                } catch (InterruptedException e) {
                }
            } else {
                return;
            }
        }
    }
}

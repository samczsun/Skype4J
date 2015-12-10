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
import com.samczsun.skype4j.internal.SkypeImpl;

public class AuthenticationChecker extends Thread {
    private SkypeImpl skype;

    public AuthenticationChecker(SkypeImpl skype) {
        super("Skype4J-Authenticator-" + skype.getUsername());
        this.skype = skype;
    }

    public void run() {
        while (skype.isLoggedIn()) {
            long diff = (skype.getExpirationTime() - System.currentTimeMillis());
            if (diff > 1800000) { //30 min
                try {
                    Thread.sleep(diff / 2);
                } catch (InterruptedException e) {
                }
            } else {
                try {
                    skype.reauthenticate();
                } catch (Exception e) {
                    MajorErrorEvent event = new MajorErrorEvent(MajorErrorEvent.ErrorSource.REAUTHENTICATING, e);
                    skype.getEventDispatcher().callEvent(event);
                    skype.shutdown();
                } finally {
                    return;
                }
            }
        }
    }
}

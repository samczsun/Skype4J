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

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.handler.ErrorHandler;
import com.samczsun.skype4j.exceptions.handler.ErrorSource;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.chat.ChatImpl;

import java.util.concurrent.atomic.AtomicBoolean;

public class TypingThread extends Thread {
    private ChatImpl chat;
    private ErrorHandler handler;
    private AtomicBoolean stop = new AtomicBoolean(false);

    public TypingThread(ChatImpl chat, ErrorHandler optionalHandler) {
        super(String.format("Skype4J-TypingThread-%s-%s", chat.getClient().getUsername(), chat.getIdentity()));
        this.chat = chat;
        this.handler = optionalHandler;
    }

    public void run() {
        while (!stop.get()) {
            if (stop.get()) {
                stopTyping();
                return;
            }

            JsonObject obj = new JsonObject();
            obj.add("content", chat.getClient().getUsername() + " is typing");
            obj.add("messagetype", "Control/Typing");
            obj.add("contenttype", "text");
            obj.add("clientmessageid", String.valueOf(System.currentTimeMillis()));

            try {
                Endpoints.SEND_MESSAGE_URL.open(chat.getClient(), chat.getIdentity())
                        .expect(201, "While sending typing notification")
                        .post(obj);
            } catch (ConnectionException e) {
                if (handler != null) {
                    handler.handle(ErrorSource.TYPING, e, false);
                }
                chat.getClient().handleError(ErrorSource.TYPING, e, false);
            }

            if (stop.get()) {
                stopTyping();
                return;
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }

            if (stop.get()) {
                stopTyping();
                return;
            }
        }
        stopTyping();
    }

    public void stopTyping() {
        if (this.stop.get()) {
            JsonObject obj = new JsonObject();
            obj.add("content", chat.getClient().getUsername() + " is done typing");
            obj.add("messagetype", "Control/ClearTyping");
            obj.add("contenttype", "text");
            obj.add("clientmessageid", String.valueOf(System.currentTimeMillis()));

            try {
                Endpoints.SEND_MESSAGE_URL.open(chat.getClient(), chat.getIdentity())
                        .expect(201, "While removing typing notification")
                        .post(obj);
            } catch (ConnectionException e) {
                if (handler != null) {
                    handler.handle(ErrorSource.TYPING, e, false);
                }
                chat.getClient().handleError(ErrorSource.TYPING, e, false);
            }
        }
    }

    public void end() {
        stop.set(true);
    }
}

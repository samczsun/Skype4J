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

package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.exceptions.SkypeException;
import org.jsoup.helper.Validate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author samczsun
 */
public enum EventType {
    NEW_MESSAGE("NewMessage") {
        @Override
        public void handle(SkypeImpl skype, JsonObject eventObj) throws SkypeException, IOException {
            JsonObject resource = eventObj.get("resource").asObject();
            String type = Utils.getString(resource, "messagetype");
            try {
                Validate.notNull(type, "Null type");
                MessageType.getByName(type).handle(skype, resource);
            } catch (Throwable t) {
                t.addSuppressed(new SkypeException(resource.toString()));
                throw t;
            }
        }
    },
    ENDPOINT_PRESENCE("EndpointPresence") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws SkypeException {

        }
    },
    USER_PRESENCE("UserPresence") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws SkypeException {

        }
    },
    CONVERSATION_UPDATE("ConversationUpdate") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws SkypeException {

        }
    },
    THREAD_UPDATE("ThreadUpdate") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws SkypeException {
            // User add and leave here 25898
        }
    };


    private static final Map<String, EventType> byValue = new HashMap<>();
    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static EventType getByName(String eventType) {
        return byValue.get(eventType);
    }

    public abstract void handle(SkypeImpl skype, JsonObject resource) throws SkypeException, IOException;

    static {
        for (EventType type : values()) {
            byValue.put(type.getValue(), type);
        }
    }
}

/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
 *
 * This file is part of Skype4J.
 *
 * Skype4J is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Skype4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Skype4J.
 * If not, see http://www.gnu.org/licenses/.
 */

package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.exceptions.SkypeException;

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
            MessageType.getByName(resource.get("messagetype").asString()).handle(skype, resource);
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

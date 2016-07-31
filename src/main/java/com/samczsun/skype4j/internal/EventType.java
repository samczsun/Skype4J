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

package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.exceptions.SkypeException;
import org.jsoup.helper.Validate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        public void handle(SkypeImpl skype, JsonObject eventObj) throws SkypeException {
            try {
                JsonObject resource = eventObj.get("resource").asObject();

                String resourceId = resource.get("id").asString();

                if (!resourceId.equals("messagingService")) {
                    throw conformError("resource.id");
                }

                String type = resource.get("type").asString();

                if (!type.equals("EndpointPresenceDoc")) {
                    throw conformError("resource.type");
                }

                String resourceLink = resource.get("selfLink").asString();

                Matcher matcher = ENDPOINT_PRESENCE_RESOURCE_LINK.matcher(resourceLink);

                if (!matcher.find()) {
                    throw conformError("resourceLink");
                }

                String id = matcher.group(1);
                String endpoint = matcher.group(2);

                JsonObject publicInfo = resource.get("publicInfo").asObject();
                JsonObject privateInfo = resource.get("privateInfo").asObject();
            } catch (Throwable t) {
                t.addSuppressed(new SkypeException(eventObj.toString()));
                throw t;
            }
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

    private static final Pattern ENDPOINT_PRESENCE_RESOURCE_LINK = Pattern.compile("\\/users\\/([^/]+)\\/endpoints\\/\\{([a-zA-Z0-9-]+)\\}\\/");


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

    private static IllegalArgumentException conformError(String object) {
        return new IllegalArgumentException(String.format("%s did not conform to format expected", object));
    }
}

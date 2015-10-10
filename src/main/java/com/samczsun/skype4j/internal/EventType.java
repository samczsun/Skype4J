package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.exceptions.SkypeException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author samczsun
 */
public enum EventType {
    NEW_MESSAGE("NewMessage") {
        @Override
        public void handle(SkypeImpl skype, JsonObject eventObj) throws SkypeException {
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

    public abstract void handle(SkypeImpl skype, JsonObject resource) throws SkypeException;

    static {
        for (EventType type : values()) {
            byValue.put(type.getValue(), type);
        }
    }
}

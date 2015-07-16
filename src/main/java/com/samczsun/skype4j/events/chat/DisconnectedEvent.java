package com.samczsun.skype4j.events.chat;

import com.samczsun.skype4j.events.Event;

import java.io.IOException;

public class DisconnectedEvent extends Event {
    private final IOException cause;

    public DisconnectedEvent(IOException cause) {
        this.cause = cause;
    }

    public IOException getCause() {
        return this.cause;
    }
}

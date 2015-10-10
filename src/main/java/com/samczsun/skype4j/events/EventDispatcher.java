package com.samczsun.skype4j.events;

public interface EventDispatcher {
    void registerListener(Listener l);
    void unregisterListener(Listener l);

    void callEvent(Event l);
}

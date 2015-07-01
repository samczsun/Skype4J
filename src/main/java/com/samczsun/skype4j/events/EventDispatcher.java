package com.samczsun.skype4j.events;

public interface EventDispatcher {
    public void registerListener(Listener l);
    public void callEvent(Event l);
}

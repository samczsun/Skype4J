package com.samczsun.skype4j.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.samczsun.skype4j.events.Event;
import com.samczsun.skype4j.events.Listener;

public class RegisteredListener {
    private final Listener listener;
    private final Method method;
    
    public RegisteredListener(Listener l, Method m) {
        this.listener = l;
        this.method = m;
        this.method.setAccessible(true);
    }
    
    public void handleEvent(Event e) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this.method.invoke(listener, e);
    }
}

package com.samczsun.skype4j.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RegisteredListener {
    private Listener listener;
    private Method method;
    
    public RegisteredListener(Listener l, Method m) {
        this.listener = l;
        this.method = m;
        this.method.setAccessible(true);
    }
    
    public void handleEvent(Event e) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this.method.invoke(listener, e);
    }
}

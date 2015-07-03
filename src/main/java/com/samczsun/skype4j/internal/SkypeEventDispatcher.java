package com.samczsun.skype4j.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.samczsun.skype4j.events.Event;
import com.samczsun.skype4j.events.EventDispatcher;
import com.samczsun.skype4j.events.EventHandler;
import com.samczsun.skype4j.events.Listener;

public class SkypeEventDispatcher implements EventDispatcher {
    private Map<Class<? extends Event>, List<RegisteredListener>> listeners = new HashMap<>();

    public void registerListener(Listener l) {
        Class<?> c = l.getClass();
        for (Method m : c.getMethods()) {
            if (m.getAnnotation(EventHandler.class) != null && m.getParameterTypes().length == 1 && Event.class.isAssignableFrom(m.getParameterTypes()[0])) {
                Class<? extends Event> eventType = m.getParameterTypes()[0].asSubclass(Event.class);
                List<RegisteredListener> methods = listeners.get(eventType);
                if (methods == null) {
                    methods = new ArrayList<>();
                    listeners.put(eventType, methods);
                }
                methods.add(new RegisteredListener(l, m));
            }
        }
    }

    public void callEvent(Event e) {
        List<RegisteredListener> methods = new ArrayList<>();
        Class<?> eventClass = e.getClass();
        while (true) {
            if (listeners.containsKey(eventClass)) {
                methods.addAll(listeners.get(eventClass));
            }
            eventClass = eventClass.getSuperclass();
            if (eventClass == Event.class) {
                break;
            }
        }
        if (methods != null) {
            for (RegisteredListener method : methods) {
                try {
                    method.handleEvent(e);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}

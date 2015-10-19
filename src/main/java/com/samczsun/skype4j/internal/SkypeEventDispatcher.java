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

import com.samczsun.skype4j.events.Event;
import com.samczsun.skype4j.events.EventDispatcher;
import com.samczsun.skype4j.events.EventHandler;
import com.samczsun.skype4j.events.Listener;
import com.samczsun.skype4j.events.error.MinorErrorEvent;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

public class SkypeEventDispatcher implements EventDispatcher {
    private SkypeImpl instance;

    public SkypeEventDispatcher(SkypeImpl instance) {
        this.instance = instance;
    }

    private final Map<Class<?>, List<RegisteredListener>> listeners = Collections.synchronizedMap(new HashMap<Class<?>, List<RegisteredListener>>());

    public void registerListener(Listener l) {
        Class<?> c = l.getClass();
        for (Method m : c.getMethods()) {
            if (m.getAnnotation(EventHandler.class) != null && m.getParameterTypes().length == 1 && Event.class.isAssignableFrom(m.getParameterTypes()[0])) {
                Class<?> eventType = m.getParameterTypes()[0];
                while (eventType != Event.class) {
                    List<RegisteredListener> methods = listeners.get(eventType);
                    if (methods == null) {
                        methods = new ArrayList<>();
                        listeners.put(eventType, methods);
                    }
                    RegisteredListener reglistener = new RegisteredListener(l, m);
                    methods.add(reglistener);
                    eventType = eventType.getSuperclass();
                }
            }
        }
    }

    public void unregisterListener(final Listener l)
    {
        for(Map.Entry<Class<? extends Event>, List<RegisteredListener>> m: listeners.entrySet())
        {
            ArrayList<RegisteredListener> toremove = new ArrayList<>();
            for(RegisteredListener rl: m.getValue())
                if (rl.isListenerEquals(l)) toremove.add(rl);
            if (toremove.size() > 0)
                m.getValue().removeAll(toremove);
        }
    }

    public void callEvent(Event e) {
        callEvent(e, true);
    }

    private void callEvent(Event e, boolean tryNotify) {
        List<RegisteredListener> methods = listeners.get(e.getClass());
        if (methods != null) {
            for (RegisteredListener method : methods) {
                try {
                    method.handleEvent(e);
                } catch (Throwable t) {
                    if (tryNotify) {
                        MinorErrorEvent event = new MinorErrorEvent();
                        callEvent(event, false);
                    }
                    instance.getLogger().log(Level.SEVERE, "Error while handling event", t);
                }
            }
        }
    }
}

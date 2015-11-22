/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
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
                        MinorErrorEvent event = new MinorErrorEvent(MinorErrorEvent.ErrorSource.DISPATCHING_EVENT, t);
                        callEvent(event, false);
                    }
                    instance.getLogger().log(Level.SEVERE, "Error while handling event", t);
                }
            }
        }
    }
}

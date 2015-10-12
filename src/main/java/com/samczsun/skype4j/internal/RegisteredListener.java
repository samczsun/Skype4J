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
import com.samczsun.skype4j.events.Listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    public boolean isListenerEquals(Listener l)
    {
        return listener == l;
    }
}

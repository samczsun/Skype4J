/*
 * Copyright 2016 Sam Sun <me@samczsun.com>
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

package com.samczsun.skype4j.events;

/**
 * Represents the event dispatcher that will dispatch events to all registered listeners
 */
public interface EventDispatcher {
    /**
     * Register a listener
     *
     * @param listener The listener to register
     */
    void registerListener(Listener listener);

    /**
     * Notify all listeners that an event has occurred
     *
     * @param event The event to call
     */
    void callEvent(Event event);
}

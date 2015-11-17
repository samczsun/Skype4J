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

package com.samczsun.skype4j.events;

/**
 * Represents a message from Skype which is not implemented within this API
 */
public class UnsupportedEvent extends Event {
    private String name;
    private final String content;


    public UnsupportedEvent(String name, String content) {
        super();
        this.name = name;
        this.content = content;
    }

    /**
     * Get the name of this event assigned by Skype
     *
     * @return The name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the content of the message from Skype
     *
     * @return The content
     */
    public String getContent() {
        return this.content;
    }
}

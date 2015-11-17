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

package com.samczsun.skype4j.events.chat;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.Event;

/**
 * Represents an event which involves a particular chat
 */
public abstract class ChatEvent extends Event {
    private final Chat chat;

    public ChatEvent(Chat c) {
        this.chat = c;
    }

    /**
     * Get the chat associated with this event
     *
     * @return The chat
     */
    public Chat getChat() {
        return chat;
    }
}

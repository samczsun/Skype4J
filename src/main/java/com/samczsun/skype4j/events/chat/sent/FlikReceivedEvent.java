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

package com.samczsun.skype4j.events.chat.sent;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.formatting.IMoji;
import com.samczsun.skype4j.participants.Participant;
import com.samczsun.skype4j.participants.User;

public class FlikReceivedEvent extends ChatEvent {
    private final Participant sender;
    private final IMoji sent;

    public FlikReceivedEvent(Chat c, Participant sender, IMoji sent) {
        super(c);
        this.sender = sender;
        this.sent = sent;
    }

    public Participant getSender() {
        return this.sender;
    }

    public IMoji getFlik() {
        return this.sent;
    }
}

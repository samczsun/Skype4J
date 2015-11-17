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

package com.samczsun.skype4j.events.chat.user.action;

import com.samczsun.skype4j.events.chat.user.UserEvent;
import com.samczsun.skype4j.user.User;

public class OptionUpdateEvent extends UserEvent {
    private long time;
    private Option option;
    private boolean enabled;

    public OptionUpdateEvent(User user, long time, Option option, boolean enabled) {
        super(user);
        this.time = time;
        this.option = option;
        this.enabled = enabled;
    }

    public long getEventTime() {
        return this.time;
    }

    public Option getOption() {
        return this.option;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public enum Option {
        JOINING_ENABLED("joiningenabled"),
        HISTORY_DISCLOSED("historydisclosed");

        private String id;

        Option(String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }
    }
}

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

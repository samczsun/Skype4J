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

    public static enum Option {
        JOINING_ENABLED,
        HISTORY_DISCLOSED;
    }
}

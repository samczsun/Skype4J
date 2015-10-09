package com.samczsun.skype4j.events.chat.user.action;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.events.chat.user.UserEvent;
import com.samczsun.skype4j.user.User;

/**
 * Called when the picture of a group chat is updated
 */
public class PictureUpdateEvent extends UserEvent {
    private long time;
    private String url;

    public PictureUpdateEvent(User initiator, long time, String url) {
        super(initiator);
        this.time = time;
        this.url = url;
    }

    public long getEventTime() {
        return this.time;
    }

    public String getPictureURL() {
        return this.url;
    }
}

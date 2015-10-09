package com.samczsun.skype4j.events.chat.user.action;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.events.chat.user.UserEvent;
import com.samczsun.skype4j.user.User;

public class TopicUpdateEvent extends UserEvent {
    private long time;
    private String newTopic;

    public TopicUpdateEvent(User initiator, long time, String newTopic) {
        super(initiator);
        this.time = time;
        this.newTopic = newTopic;
    }

    public long getEventTime() {
        return this.time;
    }

    public String getNewTopic() {
        return this.newTopic;
    }
}

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

package com.samczsun.skype4j.events.chat.sent;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.objects.ReceivedFile;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.user.User;

import java.util.List;

public class FileReceivedEvent extends ChatEvent {
    private User sender;
    private List<ReceivedFile> files;

    public FileReceivedEvent(Chat chat, User sender, List<ReceivedFile> files) {
        super(chat);
        this.sender = sender;
        this.files = files;
    }

    public User getSender() {
        return this.sender;
    }

    public List<ReceivedFile> getSentFiles() {
        return this.files;
    }
}

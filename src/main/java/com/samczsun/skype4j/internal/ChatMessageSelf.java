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

package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.SentMessage;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.Text;
import com.samczsun.skype4j.user.User;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;

public class ChatMessageSelf extends ChatMessageImpl implements SentMessage {
    private final String clientId;
    private final String id;
    private Message message;
    private final long time;
    private final User sender;

    public ChatMessageSelf(Chat chat, User user, String id, String clientId, long time, Message message) {
        this.clientId = clientId;
        this.message = message;
        this.time = time;
        this.sender = user;
        this.id = id;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public Message getContent() {
        return message;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public User getSender() {
        return sender;
    }

    @Override
    public void edit(Message newMessage) throws ConnectionException {
        try {
            JsonObject obj = new JsonObject();
            obj.add("content", newMessage.write());
            obj.add("messagetype", "RichText");
            obj.add("contenttype", "text");
            obj.add("skypeeditedid", this.clientId);
            URL url = new URL("https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + this.sender.getChat().getIdentity() + "/messages");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("RegistrationToken", ((ChatImpl) sender.getChat()).getClient().getRegistrationToken());
            con.setRequestProperty("Content-Type", "application/json");
            con.getOutputStream().write(obj.toString().getBytes());
            con.getInputStream();
        } catch (IOException e) {
            throw new ConnectionException("While editing a message", e);
        }
    }

    @Override
    public void delete() throws SkypeException {
        edit(Message.create().with(Text.BLANK));
    }

    @Override
    public Chat getChat() {
        return sender.getChat();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setContent(Message content) {
        this.message = content;
    }
}

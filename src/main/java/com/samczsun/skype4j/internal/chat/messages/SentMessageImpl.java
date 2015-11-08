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

package com.samczsun.skype4j.internal.chat.messages;

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.messages.SentMessage;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.Text;
import com.samczsun.skype4j.internal.ConnectionBuilder;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.chat.ChatImpl;
import com.samczsun.skype4j.user.User;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SentMessageImpl extends ChatMessageImpl implements SentMessage {
    public SentMessageImpl(Chat chat, User user, String id, String clientId, long time, Message message, SkypeImpl skype) {
        super(chat, user, id, clientId, time, message, skype);
    }

    @Override
    public void edit(Message newMessage) throws ConnectionException {
        try {
            JsonObject obj = new JsonObject();
            obj.add("content", newMessage.write());
            obj.add("messagetype", "RichText");
            obj.add("contenttype", "text");
            obj.add("skypeeditedid", this.getClientId());

            ConnectionBuilder builder = new ConnectionBuilder();
            builder.setUrl(String.format(Endpoints.SEND_MESSAGE_URL, this.getChat().getIdentity()));
            builder.setMethod("POST", true);
            builder.addHeader("RegistrationToken", getClient().getRegistrationToken());
            builder.setData(obj);

            HttpURLConnection con = builder.build();

            if (con.getResponseCode() != 201) {
                throw getClient().generateException("While editing a message", con);
            }
        } catch (IOException e) {
            throw getClient().generateException("While editing a message", e);
        }
    }

    @Override
    public void delete() throws ConnectionException {
        edit(Message.create().with(Text.BLANK));
    }
}

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

import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.ContactRequest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ContactRequestImpl implements ContactRequest {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private Date time;
    private Contact sender;
    private String message;

    private SkypeImpl skype;

    public ContactRequestImpl(String time, Contact sender, String message, SkypeImpl skype) throws ParseException {
        this.time = FORMAT.parse(time);
        this.sender = sender;
        this.message = message;
        this.skype = skype;
    }

    @Override
    public Date getTime() {
        return new Date(this.time.getTime());
    }

    @Override
    public Contact getSender() {
        return this.sender;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public void accept() throws ConnectionException {
        try {
            ConnectionBuilder builder = new ConnectionBuilder();
            builder.setUrl(String.format(Endpoints.ACCEPT_CONTACT_REQUEST, sender.getUsername()));
            builder.setMethod("PUT", false);
            builder.addHeader("X-Skypetoken", skype.getSkypeToken());
            HttpURLConnection connection = builder.build();
            if (connection.getResponseCode() != 201) {
                throw skype.generateException("While accepting contact request", connection);
            }
        } catch (IOException e) {
            throw skype.generateException("While accepting contact request", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContactRequestImpl that = (ContactRequestImpl) o;

        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        if (sender != null ? !sender.equals(that.sender) : that.sender != null) return false;
        return !(message != null ? !message.equals(that.message) : that.message != null);

    }

    @Override
    public int hashCode() {
        int result = time != null ? time.hashCode() : 0;
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}

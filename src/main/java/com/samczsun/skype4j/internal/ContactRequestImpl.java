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
    private final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

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

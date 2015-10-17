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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.user.Contact;
import org.jsoup.helper.Validate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class ContactImpl implements Contact {
    private static final String PROFILES_URL = "https://api.skype.com/users/self/contacts/profiles"; //contacts[] = username

    public static final Contact createContact(Skype skype, String username) throws ConnectionException, IOException {
        Validate.isTrue(skype instanceof SkypeImpl, String.format("Now is not the time to use that, %s", skype.getUsername()));
        Validate.notEmpty(username, "Username must not be empty");
        return new ContactImpl((SkypeImpl) skype, username);
    }

    private SkypeImpl skype;
    private String username;
    private String displayName;

    ContactImpl(SkypeImpl skype, String username) throws ConnectionException, IOException {
        this.skype = skype;
        this.username = username;
        ConnectionBuilder builder = new ConnectionBuilder();
        builder.setUrl(PROFILES_URL);
        builder.setMethod("POST", true);
        builder.addHeader("X-Skypetoken", skype.getSkypeToken());
        builder.setData("contacts[]=" + username);
        HttpURLConnection con = builder.build();
        if (con.getResponseCode() == 200) {
            JsonArray array = JsonArray.readFrom(new InputStreamReader(con.getInputStream()));
            JsonObject json = array.get(0).asObject();
            if (!json.get("displayname").isNull()) {
                this.displayName = json.get("displayname").asString();
            } else if (!json.get("firstname").isNull()) {
                this.displayName = json.get("firstname").asString();
                if (!json.get("lastname").isNull()) {
                    this.displayName += " " + json.get("lastname").asString();
                }
            } else if (!json.get("lastname").isNull()) {
                this.displayName = json.get("lastname").asString();
            } else {
                this.displayName = this.username;
            }
        } else {
            throw skype.generateException("While getting contact info", con);
        }
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }
}

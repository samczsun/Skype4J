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

    public static final Contact createContact(SkypeImpl skype, String username) throws ConnectionException {
        Validate.notEmpty(username, "Username must not be empty");
        return new ContactImpl(skype, username);
    }

    private Skype skype;
    private String username;
    private String displayName;

    ContactImpl(SkypeImpl skype, String username) throws ConnectionException {
        this.skype = skype;
        this.username = username;
        try {
            ConnectionBuilder builder = new ConnectionBuilder();
            builder.setUrl(PROFILES_URL);
            builder.setMethod("POST", true);
            builder.addHeader("X-Skypetoken", skype.getSkypeToken());
            builder.setData("contacts[]=" + username);
            HttpURLConnection con = builder.build();
            if (con.getResponseCode() == 200) {
                JsonArray array = JsonArray.readFrom(new InputStreamReader(con.getInputStream(), "UTF-8"));
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
                throw ExceptionHandler.generateException("While getting contact info", con);
            }
        } catch (IOException e) {
            throw ExceptionHandler.generateException("While loading", e);
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

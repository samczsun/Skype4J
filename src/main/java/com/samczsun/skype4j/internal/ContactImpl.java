package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.ConnectionBuilder;
import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.StreamUtils;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.user.Contact;
import org.jsoup.helper.Validate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class ContactImpl implements Contact {
    private static final String PROFILES_URL = "https://api.skype.com/users/self/contacts/profiles"; //contacts[] = username

    public static final Contact createContact(Skype skype, String username) throws ConnectionException {
        Validate.isTrue(skype instanceof SkypeImpl, String.format("Now is not the time to use that, %s", skype.getUsername()));
        Validate.notEmpty(username, "Username must not be empty");
        return new ContactImpl((SkypeImpl) skype, username);
    }

    private SkypeImpl skype;
    private String username;
    private String displayName;

    ContactImpl(SkypeImpl skype, String username) throws ConnectionException {
        this.skype = skype;
        this.username = username;
        ConnectionBuilder builder = new ConnectionBuilder();
        builder.setUrl(PROFILES_URL);
        builder.setMethod("POST", true);
        builder.addHeader("X-Skypetoken", skype.getSkypeToken());
        builder.setData("contacts[]=" + username);
        try {
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
                throw skype.generateException(con);
            }
        } catch (IOException e) {
            throw new ConnectionException("While fetching contact info", e);
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

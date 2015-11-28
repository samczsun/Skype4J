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
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.user.Contact;
import org.jsoup.helper.Validate;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Pattern;

public class ContactImpl implements Contact {
    private static final Pattern PHONE_NUMBER = Pattern.compile("\\+[0-9]+");
    private static final String PROFILES_URL = "https://api.skype.com/users/self/contacts/profiles"; //contacts[] = username

    public static final Contact createContact(SkypeImpl skype, String username) throws ConnectionException {
        Validate.notEmpty(username, "Username must not be empty");
        return new ContactImpl(skype, username);
    }

    private SkypeImpl skype;
    private String username;
    private String displayName;
    private String firstName;
    private String lastName;
    private String avatarURL;
    private BufferedImage avatar;
    private String mood;
    private String richMood;
    private String country;
    private String city;
    private boolean isPhone;

    ContactImpl(SkypeImpl skype, String username) throws ConnectionException {
        this.skype = skype;
        this.username = username;
        if (!PHONE_NUMBER.matcher(username).matches()) {
            try {
                ConnectionBuilder builder = new ConnectionBuilder();
                builder.setUrl(PROFILES_URL);
                builder.setMethod("POST", true);
                builder.addHeader("X-Skypetoken", skype.getSkypeToken());
                builder.setData("contacts[]=" + username);
                HttpURLConnection con = builder.build();
                if (con.getResponseCode() == 200) {
                    JsonArray array = Utils.parseJsonArray(con.getInputStream());
                    JsonObject json = array.get(0).asObject();
                    this.firstName = json.get("firstname").isNull() ? null : json.get("firstname").asString();
                    this.lastName = json.get("lastname").isNull() ? null : json.get("lastname").asString();
                    this.displayName = json.get("displayname").isNull() ? null : json.get("displayname").asString();
                    this.avatarURL = json.get("avatarUrl").isNull() ? null : json.get("avatarUrl").asString();
                    this.mood = json.get("mood").isNull() ? null : json.get("mood").asString();
                    this.richMood = json.get("richMood").isNull() ? null : json.get("richMood").asString();
                    this.country = json.get("country").isNull() ? null : json.get("country").asString();
                    this.city = json.get("city").isNull() ? null : json.get("city").asString();
                    if (this.displayName == null) {
                        if (this.firstName != null) {
                            this.displayName = this.firstName;
                            if (this.lastName != null) {
                                this.displayName = this.displayName + " " + this.lastName;
                            }
                        } else if (this.lastName != null) {
                            this.displayName = this.lastName;
                        } else {
                            this.displayName = this.username;
                        }
                    }
                } else {
                    throw ExceptionHandler.generateException("While getting contact info", con);
                }
            } catch (IOException e) {
                throw ExceptionHandler.generateException("While loading", e);
            }
        } else {
            this.isPhone = true;
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

    @Override
    public String getFirstName() {
        return this.firstName;
    }

    @Override
    public String getLastName() {
        return this.lastName;
    }

    @Override
    public BufferedImage getAvatarPicture() throws ConnectionException {
        if (this.avatarURL != null) {
            if (this.avatar == null) {
                HttpURLConnection connection = null;
                try {
                    connection = Endpoints.custom(this.avatarURL, skype).get();
                    if (connection.getResponseCode() != 200) {
                        throw ExceptionHandler.generateException("While fetching avatar", connection);
                    }
                    this.avatar = ImageIO.read(connection.getInputStream());
                } catch (IOException e) {
                    throw ExceptionHandler.generateException("While fetching avatar", e);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
            BufferedImage clone = new BufferedImage(avatar.getWidth(), avatar.getHeight(), avatar.getType());
            Graphics2D g2d = clone.createGraphics();
            g2d.drawImage(avatar, 0, 0, null);
            g2d.dispose();
            return clone;
        }
        return null;
    }

    @Override
    public String getAvatarURL() {
        return this.avatarURL;
    }

    @Override
    public String getMood() {
        return this.mood;
    }

    @Override
    public String getRichMood() {
        return this.richMood;
    }

    @Override
    public String getCountry() {
        return this.country;
    }

    @Override
    public String getCity() {
        return this.city;
    }
}

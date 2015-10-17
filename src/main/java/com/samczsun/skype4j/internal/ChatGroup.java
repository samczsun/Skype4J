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
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.events.chat.user.action.OptionUpdateEvent;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NotParticipatingException;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User;
import com.samczsun.skype4j.user.User.Role;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChatGroup extends ChatImpl implements GroupChat {
    private String topic;
    private String pictureUrl;
    private Set<OptionUpdateEvent.Option> enabledOptions = new HashSet<>();

    protected ChatGroup(SkypeImpl skype, String identity) throws ConnectionException, ChatNotFoundException, IOException {
        super(skype, identity);
    }

    protected void load() throws ConnectionException, ChatNotFoundException, IOException {
        if (isLoaded()) {
            return;
        }
        isLoading.set(true);
        Map<String, User> newUsers = new HashMap<>();

        ConnectionBuilder builder = new ConnectionBuilder();
        builder.setUrl(getClient().withCloud(CHAT_INFO_URL, getIdentity()));
        builder.addHeader("RegistrationToken", getClient().getRegistrationToken());
        builder.addHeader("Content-Type", "application/json");
        HttpURLConnection con = builder.build();

        if (con.getResponseCode() == 404) {
            throw new ChatNotFoundException();
        }
        if (con.getResponseCode() != 200) {
            throw getClient().generateException("While loading users", con);
        }
        JsonObject object = JsonObject.readFrom(new InputStreamReader(con.getInputStream()));
        JsonObject props = object.get("properties").asObject();
        for (OptionUpdateEvent.Option option : OptionUpdateEvent.Option.values()) {
            if (props.get(option.getId()) != null && props.get(option.getId()).equals("true")) {
                this.enabledOptions.add(option);
            }
        }
        if (props.get("topic") != null) {
            this.topic = props.get("topic").asString();
        } else {
            this.topic = "";
        }
        JsonArray members = object.get("members").asArray();
        for (JsonValue element : members) {
            String username = element.asObject().get("id").asString().substring(2);
            String role = element.asObject().get("role").asString();
            User user = users.get(username.toLowerCase());
            if (user == null) {
                user = new UserImpl(username, this);
            }
            newUsers.put(username.toLowerCase(), user);
            if (role.equalsIgnoreCase("admin")) {
                user.setRole(Role.ADMIN);
            } else {
                user.setRole(Role.USER);
            }
        }

        if (newUsers.get(getClient().getUsername().toLowerCase()) == null) {
            throw new NotParticipatingException();
        }

        this.users.clear();
        this.users.putAll(newUsers);
        hasLoaded.set(true);
        isLoading.set(false);
    }

    public void addUser(String username) throws ConnectionException, IOException {
        if (!users.containsKey(username.toLowerCase())) {
            User user = new UserImpl(username, this);
            users.put(username.toLowerCase(), user);
        } else if (!username.equalsIgnoreCase(getClient().getUsername())) { //Skype...
            throw new IllegalArgumentException(username + " joined the chat even though he was already in it?");
        }
    }

    public void removeUser(String username) {
        users.remove(username.toLowerCase());
    }

    public void kick(String username) throws ConnectionException, IOException {
        checkLoaded();
        ConnectionBuilder builder = new ConnectionBuilder();
        builder.setUrl(getClient().withCloud(MODIFY_MEMBER_URL, getIdentity(), username));
        builder.setMethod("DELETE", false);
        builder.addHeader("RegistrationToken", getClient().getRegistrationToken());
        HttpURLConnection con = builder.build();
        if (con.getResponseCode() != 200) {
            throw getClient().generateException("While kicking user", con);
        }
    }

    public void leave() throws ConnectionException, IOException {
        kick(getClient().getUsername());
    }

    @Override
    public String getJoinUrl() throws ConnectionException, IOException {
        checkLoaded();
        if (isOptionEnabled(OptionUpdateEvent.Option.JOINING_ENABLED)) {
            JsonObject data = new JsonObject();
            data.add("baseDomain", "https://join.skype.com/launch/");
            data.add("threadId", this.getIdentity());
            ConnectionBuilder builder = new ConnectionBuilder();
            builder.setUrl(GET_JOIN_URL);
            builder.setMethod("POST", true);
            builder.addHeader("X-Skypetoken", getClient().getSkypeToken());
            builder.addHeader("Content-Type", "application/json");
            builder.setData(data.toString());
            HttpURLConnection con = builder.build();
            if (con.getResponseCode() != 200) {
                throw getClient().generateException("While getting join URL", con);
            }
            JsonObject object = JsonObject.readFrom(new InputStreamReader(con.getInputStream()));
            return object.get("JoinUrl").asString();
        } else {
            throw new IllegalStateException("Joining is not enabled");
        }
    }

    @Override
    public String getTopic() {
        checkLoaded();
        return this.topic;
    }

    public void setTopic(String topic) throws ConnectionException, IOException  {
        checkLoaded();
        putOption("topic", JsonValue.valueOf(topic));
    }

    @Override
    public boolean isOptionEnabled(OptionUpdateEvent.Option option) {
        checkLoaded();
        return this.enabledOptions.contains(option);
    }

    @Override
    public void setOptionEnabled(OptionUpdateEvent.Option option, boolean enabled) throws ConnectionException, IOException {
        checkLoaded();
        putOption(option.getId(), JsonValue.valueOf(enabled));
        updateOption(option, enabled);
    }

    @Override
    public void add(Contact contact) throws ConnectionException, IOException {
        checkLoaded();
        JsonObject obj = new JsonObject();
        obj.add("role", "User");
        ConnectionBuilder builder = new ConnectionBuilder();
        builder.setUrl(String.format(ADD_MEMBER_URL, getIdentity(), contact.getUsername()));
        builder.setMethod("PUT", true);
        builder.addHeader("RegistrationToken", getClient().getRegistrationToken());
        builder.addHeader("Content-Type", "application/json");
        builder.setData(obj.toString());
        HttpURLConnection con = builder.build();
        if (con.getResponseCode() != 200) {
            throw getClient().generateException("While adding user into group", con);
        }
    }

    private void putOption(String option, JsonValue value) throws ConnectionException, IOException {
        JsonObject obj = new JsonObject();
        obj.add(option, value);
        ConnectionBuilder builder = new ConnectionBuilder();
        builder.setUrl(getClient().withCloud(MODIFY_PROPERTY_URL, getIdentity(), option));
        builder.setMethod("PUT", true);
        builder.addHeader("RegistrationToken", getClient().getRegistrationToken());
        builder.addHeader("Content-Type", "application/json");
        builder.setData(obj.toString());
        HttpURLConnection con = builder.build();
        if (con.getResponseCode() != 200) {
            throw getClient().generateException("While updating an option", con);
        }
    }

    public void updateTopic(String topic) {
        this.topic = topic;
    }

    public void updatePicture(String picture) {
        this.pictureUrl = picture;
    }

    public void updateOption(OptionUpdateEvent.Option option, boolean enabled) {
        if (enabled)
            enabledOptions.add(option);
        else
            enabledOptions.remove(option);
    }
}

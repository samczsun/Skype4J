package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.ConnectionBuilder;
import com.samczsun.skype4j.StreamUtils;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NotParticipatingException;
import com.samczsun.skype4j.user.User;
import com.samczsun.skype4j.user.User.Role;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class ChatGroup extends ChatImpl implements GroupChat {
    private String topic;

    protected ChatGroup(SkypeImpl skype, String identity) throws ConnectionException, ChatNotFoundException {
        super(skype, identity);
    }

    protected void load() throws ConnectionException, ChatNotFoundException {
        if (isLoaded()) {
            return;
        }
        isLoading.set(true);
        Map<String, User> newUsers = new HashMap<>();
        try {
            ConnectionBuilder builder = new ConnectionBuilder();
            builder.setUrl(getClient().withCloud(CHAT_INFO_URL, getIdentity()));
            builder.addHeader("RegistrationToken", getClient().getRegistrationToken());
            builder.addHeader("Content-Type", "application/json");
            HttpURLConnection con = builder.build();
            if (con.getResponseCode() == 200) {
                JsonObject object = JsonObject.readFrom(StreamUtils.readFully(con.getInputStream()));
                JsonObject props = object.get("properties").asObject();
                if (props.get("topic") != null) {
                    this.topic = props.get("topic").asString();
                } else {
                    this.topic = props.get("creator").asString().substring(2);
                }
                JsonArray members = object.get("members").asArray();
                for (JsonValue element : members) {
                    String username = element.asObject().get("id").asString().substring(2);
                    String role = element.asObject().get("role").asString();
                    User user = users.get(username);
                    if (user == null) {
                        user = new UserImpl(username, this);
                    }
                    newUsers.put(username, user);
                    if (role.equalsIgnoreCase("admin")) {
                        user.setRole(Role.ADMIN);
                    } else {
                        user.setRole(Role.USER);
                    }
                }
                if (newUsers.get(getClient().getUsername()) != null) {
                    hasLoaded.set(true);
                } else {
                    throw new NotParticipatingException();
                }
            } else if (con.getResponseCode() == 404) {
                throw new ChatNotFoundException();
            } else {
                throw getClient().generateException(con);
            }
        } catch (IOException e) {
            throw new ConnectionException("While loading users", e);
        } finally {
            this.users.clear();
            this.users.putAll(newUsers);
            isLoading.set(false);
        }
    }

    public void addUser(String username) throws ConnectionException {
        if (!users.containsKey(username)) {
            User user = new UserImpl(username, this);
            users.put(username, user);
        } else {
            throw new IllegalArgumentException(username + " joined the chat even though he was already in it?");
        }
    }

    public void removeUser(String username) {
        users.remove(username);
    }

    public void kick(String username) throws ConnectionException {
        checkLoaded();
        try {
            ConnectionBuilder builder = new ConnectionBuilder();
            builder.setUrl(getClient().withCloud(MODIFY_MEMBER_URL, getIdentity(), username));
            builder.setMethod("DELETE", false);
            builder.addHeader("RegistrationToken", getClient().getRegistrationToken());
            HttpURLConnection con = builder.build();
            if (con.getResponseCode() != 200) {
                throw getClient().generateException(con);
            }
        } catch (IOException e) {
            throw new ConnectionException("While kicking", e);
        }
    }

    public void leave() throws ConnectionException {
        kick(getClient().getUsername());
    }

    @Override
    public String getTopic() {
        checkLoaded();
        return this.topic;
    }

    public void setTopic(String topic) throws ConnectionException {
        checkLoaded();
        try {
            ;
            JsonObject obj = new JsonObject();
            obj.add("topic", topic);
            ConnectionBuilder builder = new ConnectionBuilder();
            builder.setUrl(getClient().withCloud(MODIFY_PROPERTY_URL, getIdentity(), "topic"));
            builder.setMethod("PUT", true);
            builder.addHeader("RegistrationToken", getClient().getRegistrationToken());
            builder.addHeader("Content-Type", "application/json");
            builder.setData(obj.toString());
            HttpURLConnection con = builder.build();
            if (con.getResponseCode() != 200) {
                throw getClient().generateException(con);
            }
        } catch (IOException e) {
            throw new ConnectionException("While updating the topic", e);
        }
    }

    public void updateTopic(String topic) {
        this.topic = topic;
    }
}

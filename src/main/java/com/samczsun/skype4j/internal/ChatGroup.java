package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.StreamUtils;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NotLoadedException;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.user.User;
import com.samczsun.skype4j.user.User.Role;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

public class ChatGroup extends ChatImpl implements GroupChat {
    private String topic;

    protected ChatGroup(SkypeImpl skype, String identity) throws SkypeException {
        super(skype, identity);
    }

    protected void load() throws ConnectionException {
        if (isLoaded()) {
            return;
        }
        isLoading.set(true);
        Map<String, User> newUsers = new HashMap<>();
        try {
            URL url = new URL("https://client-s.gateway.messenger.live.com/v1/threads/" + this.getIdentity() + "?view=msnp24Equivalent");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestProperty("RegistrationToken", getClient().getRegistrationToken());
            con.setRequestProperty("Content-Type", "application/json");
            String in = StreamUtils.readFully(con.getInputStream());
            JsonObject object = JsonObject.readFrom(in);
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
            hasLoaded.set(true);
        } catch (IOException e) {
            throw new ConnectionException("While loading users", e);
        } finally {
            this.users.clear();
            this.users.putAll(newUsers);
            isLoading.set(false);
        }
    }

    @Override
    public ChatMessage sendMessage(Message message) throws ConnectionException {
        checkLoaded();
        try {
            long ms = System.currentTimeMillis();
            JsonObject obj = new JsonObject();
            obj.add("content", message.write());
            obj.add("messagetype", "RichText");
            obj.add("contenttype", "text");
            obj.add("clientmessageid", String.valueOf(ms));
            URL url = new URL("https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + this.getIdentity() + "/messages");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("RegistrationToken", getClient().getRegistrationToken());
            con.setRequestProperty("Content-Type", "application/json");
            con.getOutputStream().write(obj.toString().getBytes(Charset.forName("UTF-8")));
            con.getInputStream();
            return ChatMessageImpl.createMessage(this, getUser(getClient().getUsername()), null, String.valueOf(ms), ms, message);
        } catch (IOException e) {
            throw new ConnectionException("While sending a message", e);
        }
    }

    @Override
    public Collection<User> getAllUsers() throws NotLoadedException {
        checkLoaded();
        return Collections.unmodifiableCollection(users.values());
    }

    public void addUser(String username) {
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
            URL url = new URL("https://client-s.gateway.messenger.live.com/v1/threads/" + this.getIdentity() + "/members/8:" + username);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("DELETE");
            con.setRequestProperty("RegistrationToken", getClient().getRegistrationToken());
            con.setRequestProperty("Content-Type", "application/json");
            con.getInputStream();
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
            URL url = new URL("https://client-s.gateway.messenger.live.com/v1/threads/" + this.getIdentity() + "/properties?name=topic");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("PUT");
            con.setDoOutput(true);
            con.setRequestProperty("RegistrationToken", getClient().getRegistrationToken());
            con.setRequestProperty("Content-Type", "application/json");
            JsonObject obj = new JsonObject();
            obj.add("topic", topic);
            con.getOutputStream().write(obj.toString().getBytes(Charset.forName("UTF-8")));
            con.getOutputStream();
        } catch (IOException e) {
            throw new ConnectionException("While updating the topic", e);
        }
    }

    public void updateTopic(String topic) {
        this.topic = topic;
    }

    public void onMessage(ChatMessage message) {
        this.messages.add(message);
        ((UserImpl) message.getSender()).onMessage(message);
    }

    @Override
    public User getUser(String username) {
        checkLoaded();
        return this.users.get(username);
    }

    @Override
    public List<ChatMessage> getAllMessages() {
        checkLoaded();
        return Collections.unmodifiableList(this.messages);
    }
}

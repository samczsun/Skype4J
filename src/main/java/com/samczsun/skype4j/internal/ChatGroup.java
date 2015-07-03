package com.samczsun.skype4j.internal;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.StreamUtils;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Text;
import com.samczsun.skype4j.user.User;
import com.samczsun.skype4j.user.User.Role;

public class ChatGroup extends ChatImpl {
    private AtomicBoolean isLoading = new AtomicBoolean(false);

    private String topic;

    private Map<String, User> users = new ConcurrentHashMap<>();
    private List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    private Map<String, ChatMessage> messageMap = new ConcurrentHashMap<>();

    protected ChatGroup(SkypeImpl skype, String identity) {
        super(skype, identity);
    }

    @Override
    public ChatMessage sendMessage(Text message) throws SkypeException {
        HttpsURLConnection con = null;
        try {
            long ms = System.currentTimeMillis();
            JsonObject obj = new JsonObject();
            obj.add("content", message.parent().write());
            obj.add("messagetype", "RichText");
            obj.add("contenttype", "text");
            obj.add("clientmessageid", String.valueOf(ms));
            URL url = new URL("https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + this.getIdentity() + "/messages");
            con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("RegistrationToken", getClient().getRegistrationToken());
            con.setRequestProperty("Content-Type", "application/json");
            con.getOutputStream().write(obj.toString().getBytes(Charset.forName("UTF-8")));
            con.getInputStream();
            return ChatMessageImpl.createMessage(this, getUser(getClient().getUsername()), null, String.valueOf(ms), ms, Jsoup.parse(message.parent().write()).text());
        } catch (IOException e) {
            throw new SkypeException("An error occured while sending a message", e);
        }
    }

    @Override
    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    public void updateUsers() throws SkypeException {
        if (isLoading.get()) {
            return;
        }
        Map<String, User> newUsers = new HashMap<>();
        isLoading.set(true);
        HttpsURLConnection con = null;
        try {
            URL url = new URL("https://client-s.gateway.messenger.live.com/v1/threads/" + this.getIdentity() + "?view=msnp24Equivalent");
            con = (HttpsURLConnection) url.openConnection();
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
                User user = getUser(username);
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
        } catch (IOException e) {
            throw new SkypeException("An exception occured while loading users", e);
        }
        this.users.clear();
        this.users.putAll(newUsers);
        isLoading.set(false);
    }

    public void addUser(String username) {
        if (!users.containsKey(username)) {
            User user = new UserImpl(username, this);
            users.put(username, user);
        } else {
            System.out.println(username + " joined twice???");
        }
    }

    public void removeUser(String username) {
        users.remove(username);
    }

    public void kick(String username) throws SkypeException {
        HttpsURLConnection con = null;
        try {
            URL url = new URL("https://getClient()-s.gateway.messenger.live.com/v1/threads/" + this.getIdentity() + "/members/8:" + username);
            con = (HttpsURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("DELETE");
            con.setRequestProperty("RegistrationToken", getClient().getRegistrationToken());
            con.setRequestProperty("Content-Type", "application/json");
            con.getInputStream();
        } catch (Exception e) {
            throw new SkypeException("An exception occured while kicking", e);
        }
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) throws SkypeException {
        HttpsURLConnection con = null;
        try {
            URL url = new URL("https://client-s.gateway.messenger.live.com/v1/threads/" + this.getIdentity() + "/properties?name=topic");
            con = (HttpsURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("PUT");
            con.setDoOutput(true);
            con.setRequestProperty("RegistrationToken", getClient().getRegistrationToken());
            con.setRequestProperty("Content-Type", "application/json");
            JsonObject obj = new JsonObject();
            obj.add("topic", topic);
            con.getOutputStream().write(obj.toString().getBytes(Charset.forName("UTF-8")));
            con.getOutputStream();
        } catch (Exception e) {
            throw new SkypeException("An exception occured while updating the topic", e);
        }
    }

    public void updateTopic(String topic) {
        this.topic = topic;
    }

    public void onMessage(ChatMessage message) {
        this.messages.add(message);
        this.messageMap.put(message.getId(), message);
        ((UserImpl) message.getSender()).onMessage(message);
    }

    @Override
    public Type getType() {
        return Type.GROUP;
    }

    @Override
    public User getUser(String username) {
        return this.users.get(username);
    }

    @Override
    public ChatMessage getMessage(String id) {
        return messageMap.get(id);
    }

    @Override
    public List<ChatMessage> getAllMessages() {
        return Collections.unmodifiableList(this.messages);
    }
}

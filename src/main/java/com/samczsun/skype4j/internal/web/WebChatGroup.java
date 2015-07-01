package com.samczsun.skype4j.internal.web;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.samczsun.skype4j.StreamUtils;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.chat.User;
import com.samczsun.skype4j.chat.User.Role;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Text;

public class WebChatGroup extends WebChat {
    private AtomicBoolean isLoading = new AtomicBoolean(false);

    private String topic;

    private List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    private Map<String, User> users = new ConcurrentHashMap<>();

    protected WebChatGroup(WebSkype skype, String identity) {
        super(skype, identity);
    }

    @Override
    public ChatMessage sendMessage(Text message) throws SkypeException {
        HttpsURLConnection con = null;
        try {
            long ms = System.currentTimeMillis();
            JsonObject obj = new JsonObject();
            obj.addProperty("content", message.parent().write());
            obj.addProperty("messagetype", "RichText");
            obj.addProperty("contenttype", "text");
            obj.addProperty("clientmessageid", String.valueOf(ms));
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            URL url = new URL("https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + this.getIdentity() + "/messages");
            con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("RegistrationToken", getClient().getRegistrationToken());
            con.setRequestProperty("Content-Type", "application/json");
            con.getOutputStream().write(gson.toJson(obj).getBytes(Charset.forName("UTF-8")));
            con.getInputStream();
            return WebChatMessage.createMessage(this, getUser(getClient().getUsername()), null, String.valueOf(ms), ms, Jsoup.parse(message.parent().write()).text());
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
            Gson gson = new Gson();
            JsonObject object = gson.fromJson(in, JsonObject.class);
            JsonObject props = object.get("properties").getAsJsonObject();
            if (props.has("topic")) {
                this.topic = props.get("topic").getAsString();
            } else {
                this.topic = props.get("creator").getAsString().substring(2);
            }
            JsonArray members = object.get("members").getAsJsonArray();
            for (JsonElement element : members) {
                String username = element.getAsJsonObject().get("id").getAsString().substring(2);
                String role = element.getAsJsonObject().get("role").getAsString();
                User user = getUser(username);
                if (user == null) {
                    user = new WebUser(username, this);
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
            User user = new WebUser(username, this);
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
            Gson gson = new Gson();
            JsonObject obj = new JsonObject();
            obj.addProperty("topic", topic);
            con.getOutputStream().write(gson.toJson(obj).getBytes(Charset.forName("UTF-8")));
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
    }

    @Override
    public Type getType() {
        return Type.GROUP;
    }

    @Override
    public User getUser(String username) {
        return this.users.get(username);
    }
}

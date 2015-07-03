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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Text;
import com.samczsun.skype4j.user.User;

public class ChatIndividual extends ChatImpl {
    private AtomicBoolean isLoading = new AtomicBoolean(false);

    private final Type type = Type.INDIVIDUAL;

    private String topic;

    private Map<String, User> users = new ConcurrentHashMap<>();
    private List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    private Map<String, ChatMessage> messageMap = new ConcurrentHashMap<>();

    protected ChatIndividual(SkypeImpl skype, String identity) {
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
            return ChatMessageImpl.createMessage(this, getUser(getClient().getUsername()), null, String.valueOf(ms), ms, Jsoup.parse(message.parent().write()).text());
        } catch (IOException e) {
            throw new SkypeException("An exception occured while sending a message", e);
        }
    }

    @Override
    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    public void updateUsers() {
        if (isLoading.get()) {
            return;
        }
        isLoading.set(true);
        Map<String, User> newUsers = new HashMap<>();
        String username = this.getIdentity().substring(2);
        this.topic = username;
        User user = getUser(username);
        if (user == null) {
            user = new UserImpl(username, this);
        }
        newUsers.put(username, user);
        User me = getUser(getClient().getUsername());
        if (me == null) {
            me = new UserImpl(getClient().getUsername(), this);
            newUsers.put(getClient().getUsername(), me);
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

    public boolean kick(String username) {
        throw new IllegalArgumentException("Cannot kick in individual chats");
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        throw new IllegalArgumentException("Cannot set topic in individual chats");
    }

    public void onMessage(ChatMessage message) {
        this.messages.add(message);
        this.messageMap.put(message.getId(), message);
        ((UserImpl) message.getSender()).onMessage(message);
    }

    @Override
    public Type getType() {
        return this.type;
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
        return Collections.unmodifiableList(messages);
    }
}

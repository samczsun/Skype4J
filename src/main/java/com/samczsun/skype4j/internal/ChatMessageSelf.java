package com.samczsun.skype4j.internal;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.Text;
import com.samczsun.skype4j.user.User;

public class ChatMessageSelf extends ChatMessageImpl {
    private String clientId;
    private String id;
    private String message;
    private long time;
    private User sender;

    public ChatMessageSelf(Chat chat, User user, String id, String clientId, long time, String message) {
        this.clientId = clientId;
        this.message = message;
        this.time = time;
        this.sender = user;
        this.id = id;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getText() {
        return message;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public User getSender() {
        return sender;
    }

    @Override
    public void edit(Text newMessage) throws SkypeException {
        HttpsURLConnection con = null;
        try {
            JsonObject obj = new JsonObject();
            obj.add("content", newMessage.parent().write());
            obj.add("messagetype", "RichText");
            obj.add("contenttype", "text");
            obj.add("skypeeditedid", this.clientId);
            URL url = new URL("https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + this.sender.getChat().getIdentity() + "/messages");
            con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("RegistrationToken", ((ChatImpl) sender.getChat()).getClient().getRegistrationToken());
            con.setRequestProperty("Content-Type", "application/json");
            con.getOutputStream().write(obj.toString().getBytes());
            con.getInputStream();
        } catch (Exception e) {
            throw new SkypeException("An exception occured while editing a message", e);
        }
    }

    @Override
    public void delete() throws SkypeException {
        edit(Message.text(""));
    }

    @Override
    public Chat getChat() {
        return sender.getChat();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setContent(String content) {
        this.message = content;
    }
}

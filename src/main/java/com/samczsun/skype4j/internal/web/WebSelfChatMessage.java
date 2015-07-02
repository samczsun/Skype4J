package com.samczsun.skype4j.internal.web;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.chat.User;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.Text;

public class WebSelfChatMessage implements ChatMessage {
    private String clientId;
    private String message;
    private long time;
    private User sender;

    public WebSelfChatMessage(Chat chat, User user, String id, String clientId, long time, String message) {
        this.clientId = clientId;
        this.message = message;
        this.time = time;
        this.sender = user;
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
            obj.addProperty("content", newMessage.parent().write());
            obj.addProperty("messagetype", "RichText");
            obj.addProperty("contenttype", "text");
            obj.addProperty("skypeeditedid", this.clientId);
            Gson gson = new Gson();
            URL url = new URL("https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + this.sender.getChat().getIdentity() + "/messages");
            con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("RegistrationToken", ((WebChat) sender.getChat()).getClient().getRegistrationToken());
            con.setRequestProperty("Content-Type", "application/json");
            con.getOutputStream().write(gson.toJson(obj).getBytes());
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
}

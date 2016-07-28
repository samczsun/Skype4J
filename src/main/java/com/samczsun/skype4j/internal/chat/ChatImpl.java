/*
 * Copyright 2016 Sam Sun <me@samczsun.com>
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

package com.samczsun.skype4j.internal.chat;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.handler.ErrorHandler;
import com.samczsun.skype4j.formatting.IMoji;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.Text;
import com.samczsun.skype4j.internal.*;
import com.samczsun.skype4j.internal.chat.messages.ChatMessageImpl;
import com.samczsun.skype4j.internal.participants.ParticipantImpl;
import com.samczsun.skype4j.internal.participants.UserImpl;
import com.samczsun.skype4j.internal.threads.TypingThread;
import com.samczsun.skype4j.participants.Participant;
import com.samczsun.skype4j.participants.info.Contact;
import com.samczsun.skype4j.participants.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;

public abstract class ChatImpl implements Chat {
    protected final Map<String, ParticipantImpl> users = Collections.synchronizedMap(new HashMap<>());
    protected final List<ChatMessage> messages = new CopyOnWriteArrayList<>();

    private final SkypeImpl client;
    private final String identity;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    private String backwardLink;
    private String syncState;

    private TypingThread typingThread;

    ChatImpl(SkypeImpl client, String identity) throws ConnectionException, ChatNotFoundException {
        this.client = client;
        this.identity = identity;
    }

    @Override
    public ChatMessage sendMessage(Message message) throws ConnectionException {
        long ms = System.currentTimeMillis();

        JsonObject obj = new JsonObject();
        obj.add("content", message.write());
        obj.add("messagetype", "RichText");
        obj.add("contenttype", "text");
        obj.add("clientmessageid", String.valueOf(ms));

        Endpoints.SEND_MESSAGE_URL.open(getClient(), getIdentity()).expect(201, "While sending message").post(obj);

        return Factory.createMessage(this, getSelf(), null, String.valueOf(ms), ms, message,
                getClient());
    }

    @Override
    public ChatMessage sendMessage(String plainMessage) throws ConnectionException {
        return sendMessage(Message.create().with(Text.plain(plainMessage)));
    }

    @Override
    public void sendContact(Contact contact) throws ConnectionException {
        long ms = System.currentTimeMillis();

        JsonObject obj = new JsonObject();
        obj.add("content", String.format("<contacts><c t=\"s\" s=\"%s\" f=\"%s\"/></contacts>", contact.getUsername(),
                contact.getDisplayName()));
        obj.add("messagetype", "RichText/Contacts");
        obj.add("contenttype", "text");
        obj.add("clientmessageid", String.valueOf(ms));

        Endpoints.SEND_MESSAGE_URL.open(getClient(), getIdentity()).expect(201, "While sending message").post(obj);
    }

    @Override
    public void sendImage(BufferedImage image, String imageType, String imageName) throws ConnectionException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, imageType, baos);
        sendImage(baos.toByteArray(), imageName);
    }

    @Override
    public void sendImage(File image) throws ConnectionException, IOException {
        byte[] data = Files.readAllBytes(image.toPath());
        String name = image.getName().substring(0, image.getName().lastIndexOf('.'));
        sendImage(data, name);
    }

    private void sendImage(byte[] data, String imageName) throws ConnectionException, IOException {
        String id = Utils.uploadImage(data, Utils.ImageType.IMGT1, this);
        long ms = System.currentTimeMillis();
        String content = "<URIObject type=\"Picture.1\" uri=\"https://api.asm.skype.com/v1/objects/%s\" url_thumbnail=\"https://api.asm.skype.com/v1/objects/%s/views/imgt1\">MyLegacy pish <a href=\"https://api.asm.skype.com/s/i?%s\">https://api.asm.skype.com/s/i?%s</a><Title/><Description/><OriginalName v=\"%s\"/><meta type=\"photo\" originalName=\"%s\"/></URIObject>";
        content = String.format(content, id, id, id, id, imageName, imageName);
        JsonObject obj = new JsonObject();
        obj.add("content", content);
        obj.add("messagetype", "RichText/UriObject");
        obj.add("contenttype", "text");
        obj.add("clientmessageid", String.valueOf(ms));

        Endpoints.SEND_MESSAGE_URL.open(getClient(), getIdentity()).expect(201, "While sending message").post(obj);
    }

    @Override
    public void sendFile(File file) throws ConnectionException {
        try {
            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamUtils.copy(in, out);
            String id = Utils.upload(out.toByteArray(), Utils.ImageType.FILE,
                    new JsonObject().add("filename", file.getName()), this);
            long ms = System.currentTimeMillis();
            String content = "<URIObject type=\"File.1\" uri=\"https://api.asm.skype.com/v1/objects/%s\" url_thumbnail=\"https://api.asm.skype.com/v1/objects/%s/views/thumbnail\"><Title>Title: %s</Title><Description> Description: %s</Description><a href=\"https://login.skype.com/login/sso?go=webclient.xmm&amp;docid=%s\"> https://login.skype.com/login/sso?go=webclient.xmm&amp;docid=%s</a><OriginalName v=\"%s\"/><FileSize v=\"%s\"/></URIObject>";
            content = String.format(content, id, id, file.getName(), file.getName(), id, id, file.getName(),
                    out.size());
            JsonObject obj = new JsonObject();
            obj.add("content", content);
            obj.add("messagetype", "RichText/Media_GenericFile");
            obj.add("contenttype", "text");
            obj.add("clientmessageid", String.valueOf(ms));

            Endpoints.SEND_MESSAGE_URL.open(getClient(), getIdentity()).expect(201, "While sending message").post(obj);
        } catch (IOException e) {
            throw ExceptionHandler.generateException("While sending message", e);
        }
    }

    @Override
    public void sendMoji(IMoji flik) throws ConnectionException {
        long ms = System.currentTimeMillis();
        String content = "<URIObject type=\"Video.1/Flik.1\" uri=\"https://static.asm.skype.com/pes/v1/items/%s\" url_thumbnail=\"https://static.asm.skype.com/pes/v1/items/%s/views/thumbnail\"><a href=\"https://static.asm.skype.com/pes/v1/items/%s/views/default\">https://static.asm.skype.com/pes/v1/items/%s/views/default</a><OriginalName v=\"\"/></URIObject>";
        content = String.format(content, flik.getId(), flik.getId(), flik.getId(), flik.getId());
        JsonObject obj = new JsonObject();
        obj.add("content", content);
        obj.add("messagetype", "RichText/Media_FlikMsg");
        obj.add("contenttype", "text");
        obj.add("clientmessageid", String.valueOf(ms));

        Endpoints.SEND_MESSAGE_URL.open(getClient(), getIdentity()).expect(201, "While sending message").post(obj);
    }

    @Override
    public List<ChatMessage> loadMoreMessages(int amount) throws ConnectionException {
        JsonObject data;
        if (backwardLink == null) {
            if (syncState == null) {
                data = Endpoints.LOAD_MESSAGES
                        .open(getClient(), getIdentity(), amount)
                        .as(JsonObject.class)
                        .expect(200, "While loading messages")
                        .get();
            } else {
                return Collections.emptyList();
            }
        } else {
            Matcher matcher = SkypeImpl.PAGE_SIZE_PATTERN.matcher(this.backwardLink);
            //Matcher find appears to be doing nothing.
            matcher.find();
            String url = matcher.replaceAll("pageSize=" + amount);
            data =  Endpoints
                    .custom(url, getClient())
                    .header("RegistrationToken", getClient().getRegistrationToken())
                    .as(JsonObject.class)
                    .expect(200, "While loading messages")
                    .get();
        }
        List<ChatMessage> messages = new ArrayList<>();

        for (JsonValue value : data.get("messages").asArray()) {
            try {
                JsonObject msg = value.asObject();
                if (msg.get("messagetype").asString().equals("RichText") || msg.get("messagetype").asString().equals("Text")) {
                    UserImpl u = (UserImpl) MessageType.getUser(msg.get("from").asString(), this);
                    Message message = Message.fromHtml(MessageType.stripMetadata(msg.get("content").asString()));
                    if (msg.get("clientmessageid") != null) {
                        ChatMessage m = Factory.createMessage(this, u, msg.get("id").asString(),
                                msg.get("clientmessageid").asString(),
                                formatter.parse(msg.get("originalarrivaltime").asString()).getTime(), message
                                ,getClient());
                        this.messages.add(0, m);
                        u.insertMessage(m, 0);
                        messages.add(m);
                    } else {
                        ChatMessageImpl chatMessage = (ChatMessageImpl) u.getMessageById(msg.get("skypeeditedid").asString());
                        if (chatMessage != null) {
                            chatMessage.edit0(message);
                        }
                    }
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        JsonObject metadata = data.get("_metadata").asObject();
        if (metadata.get("backwardLink") != null) {
            this.backwardLink = metadata.get("backwardLink").asString();
        } else {
            this.backwardLink = null;
        }
        this.syncState = metadata.get("syncState").asString();
        return messages;
    }

    @Override
    public Collection<Participant> getAllParticipants() {
        return Collections.unmodifiableCollection(users.values());
    }

    @Override
    public ParticipantImpl getParticipant(String username) {
        return this.users.get(username.toLowerCase());
    }

    @Override
    public UserImpl getSelf() {
        return (UserImpl) getParticipant(getClient().getId());
    }

    @Override
    public List<ChatMessage> getAllMessages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public String getIdentity() {
        return this.identity;
    }

    @Override
    public SkypeImpl getClient() {
        return this.client;
    }

    @Override
    public void startTyping(ErrorHandler handler) {
        if (this.typingThread == null) {
            this.typingThread = new TypingThread(this, handler);
            this.typingThread.start();
        }
    }

    @Override
    public void stopTyping() {
        if (this.typingThread != null) {
            this.typingThread.end();
            this.typingThread = null;
        }
    }

    @Override
    public void sync() throws ConnectionException {
    }

    // Begin internal access methods

    public void onMessage(ChatMessageImpl message) {
        this.messages.add(message);
        message.getSender().onMessage(message);
    }

    public void alertsOff() throws ConnectionException {
        putOption("alerts", JsonValue.valueOf(false), false);
    }

    public void alertsOn() throws ConnectionException {
        alertsOn(null);
    }

    public void alertsOn(String keyword) throws ConnectionException {
        putOption("alerts", JsonValue.valueOf(true), false);
        putOption("alertmatches", JsonValue.valueOf(keyword), false);
    }

    public abstract void addUser(String username) throws ConnectionException;

    public abstract void removeUser(String username);

    public abstract void load() throws ConnectionException, ChatNotFoundException;

    protected void putOption(String option, JsonValue value, boolean global) throws ConnectionException {
        JsonObject obj = new JsonObject();
        obj.add(option, value);
        (global ? Endpoints.CONVERSATION_PROPERTY_GLOBAL : Endpoints.CONVERSATION_PROPERTY_SELF)
                .open(getClient(), getIdentity(), option)
                .expect(200, "While updating option")
                .put(obj);
    }
}

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

package com.samczsun.skype4j.internal.chat;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NotLoadedException;
import com.samczsun.skype4j.formatting.IMoji;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.Text;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.ExceptionHandler;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.StreamUtils;
import com.samczsun.skype4j.internal.UserImpl;
import com.samczsun.skype4j.internal.Utils;
import com.samczsun.skype4j.internal.chat.messages.ChatMessageImpl;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User;
import org.jsoup.helper.Validate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ChatImpl implements Chat {
    protected final AtomicBoolean isLoading = new AtomicBoolean(false);
    protected final AtomicBoolean hasLoaded = new AtomicBoolean(false);

    protected final Map<String, UserImpl> users = new ConcurrentHashMap<>();
    protected final List<ChatMessage> messages = new CopyOnWriteArrayList<>();

    private final SkypeImpl client;
    private final String identity;

    ChatImpl(SkypeImpl client, String identity) throws ConnectionException, ChatNotFoundException {
        this.client = client;
        this.identity = identity;
        load();
    }

    @Override
    public ChatMessage sendMessage(Message message) throws ConnectionException {
        checkLoaded();
        long ms = System.currentTimeMillis();

        JsonObject obj = new JsonObject();
        obj.add("content", message.write());
        obj.add("messagetype", "RichText");
        obj.add("contenttype", "text");
        obj.add("clientmessageid", String.valueOf(ms));

        Endpoints.SEND_MESSAGE_URL.open(getClient(), getIdentity()).expect(201, "While sending message").post(obj);

        return ChatMessageImpl.createMessage(this, getUser(client.getUsername()), null, String.valueOf(ms), ms, message,
                getClient());
    }

    @Override
    public ChatMessage sendMessage(String plainMessage) throws ConnectionException {
        return sendMessage(Message.create().with(Text.plain(plainMessage)));
    }

    @Override
    public void sendContact(Contact contact) throws ConnectionException {
        checkLoaded();
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
        checkLoaded();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, imageType, baos);
        sendImage(baos.toByteArray(), imageName);
    }

    @Override
    public void sendImage(File image) throws ConnectionException, IOException {
        checkLoaded();
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
        checkLoaded();
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
        checkLoaded();
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
    public Collection<User> getAllUsers() {
        checkLoaded();
        return Collections.unmodifiableCollection(users.values());
    }

    @Override
    public UserImpl getUser(String username) {
        checkLoaded();
        return this.users.get(username.toLowerCase());
    }

    @Override
    public User getSelf() {
        return getUser(getClient().getUsername());
    }

    @Override
    public List<ChatMessage> getAllMessages() {
        checkLoaded();
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

    // Begin internal access methods
    public static ChatImpl createChat(SkypeImpl client, String identity) throws ConnectionException, ChatNotFoundException {
        Validate.notNull(client, "Client must not be null");
        Validate.notEmpty(identity, "Identity must not be null/empty");
        if (identity.startsWith("19:")) {
            if (identity.endsWith("@thread.skype")) {
                return new ChatGroup(client, identity);
            } else {
                throw new IllegalArgumentException(String.format("Cannot load P2P chat with identity %s", identity));
            }
        } else if (identity.startsWith("8:")) {
            return new ChatIndividual(client, identity);
        } else {
            throw new IllegalArgumentException(String.format("Unknown chat type with identity %s", identity));
        }
    }

    public void onMessage(ChatMessage message) {
        this.messages.add(message);
        ((UserImpl) message.getSender()).onMessage(message);
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

    public boolean isLoaded() {
        return !isLoading.get() && hasLoaded.get();
    }

    public abstract void addUser(String username) throws ConnectionException;

    public abstract void removeUser(String username);

    protected abstract void load() throws ConnectionException, ChatNotFoundException;

    protected void checkLoaded() {
        if (!isLoaded()) {
            throw new NotLoadedException();
        }
    }

    protected void putOption(String option, JsonValue value, boolean global) throws ConnectionException {
        JsonObject obj = new JsonObject();
        obj.add(option, value);
        (global ? Endpoints.CONVERSATION_PROPERTY_GLOBAL : Endpoints.CONVERSATION_PROPERTY_SELF)
                .open(getClient(), getIdentity(), option)
                .expect(200, "While updating option")
                .put(obj);
    }
}

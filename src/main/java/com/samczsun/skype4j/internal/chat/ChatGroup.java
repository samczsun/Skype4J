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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.events.chat.user.action.OptionUpdateEvent;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.MessageType;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.UserImpl;
import com.samczsun.skype4j.internal.Utils;
import com.samczsun.skype4j.internal.chat.messages.ChatMessageImpl;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User.Role;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

public class ChatGroup extends ChatImpl implements GroupChat {
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    private String topic;
    private String pictureUrl;
    private boolean pictureUpdated;
    private BufferedImage picture;
    private String backwardLink;
    private String syncState;
    private Set<OptionUpdateEvent.Option> enabledOptions;

    protected ChatGroup(SkypeImpl skype, String identity) throws ConnectionException, ChatNotFoundException {
        super(skype, identity);
    }

    protected void load() throws ConnectionException, ChatNotFoundException {
        if (isLoaded()) {
            return;
        }
        enabledOptions = new HashSet<>();
        try {
            isLoading.set(true);
            Map<String, UserImpl> newUsers = new HashMap<>();
            JsonObject object = Endpoints.CHAT_INFO_URL
                    .open(getClient(), getIdentity())
                    .as(JsonObject.class)
                    .on(404, (connection) -> {throw new ChatNotFoundException();})
                    .expect(200, "While loading users")
                    .get();

            JsonObject props = object.get("properties").asObject();
            for (OptionUpdateEvent.Option option : OptionUpdateEvent.Option.values()) {
                if (props.get(option.getId()) != null && props.get(option.getId()).asString().equals("true")) {
                    this.enabledOptions.add(option);
                }
            }
            if (props.get("topic") != null) {
                this.topic = props.get("topic").asString();
            } else {
                this.topic = "";
            }
            if (props.get("picture") != null) {
                this.pictureUrl = props.get("picture").asString().substring(4);
            }
            JsonArray members = object.get("members").asArray();
            for (JsonValue element : members) {
                String username = element.asObject().get("id").asString().substring(2);
                String role = element.asObject().get("role").asString();
                UserImpl user = users.get(username.toLowerCase());
                if (user == null) {
                    user = new UserImpl(username, this, getClient());
                }
                newUsers.put(username.toLowerCase(), user);
                if (role.equalsIgnoreCase("admin")) {
                    user.updateRole(Role.ADMIN);
                } else {
                    user.updateRole(Role.USER);
                }
            }

            this.users.clear();
            this.users.putAll(newUsers);
            hasLoaded.set(true);
        } finally {
            isLoading.set(false);
        }
    }

    public List<ChatMessage> loadMoreMessages(int amount) throws ConnectionException {
        checkLoaded();
        JsonObject data = null;
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
            matcher.find();
            String url = matcher.replaceAll("pageSize=" + amount);
            data = Endpoints
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
                if (msg.get("messagetype").asString().equals("RichText")) {
                    UserImpl u = (UserImpl) MessageType.getUser(msg.get("from").asString(), this);
                    ChatMessage m = ChatMessageImpl.createMessage(this, u, msg.get("id").asString(),
                            msg.get("id").asString(),
                            formatter.parse(msg.get("originalarrivaltime").asString()).getTime(),
                            Message.fromHtml(MessageType.stripMetadata(msg.get("content").asString())), getClient());
                    this.messages.add(0, m);
                    u.insertMessage(m, 0);
                    messages.add(m);
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

    public void addUser(String username) throws ConnectionException {
        if (!users.containsKey(username.toLowerCase())) {
            UserImpl user = new UserImpl(username, this, getClient());
            users.put(username.toLowerCase(), user);
        } else if (!username.equalsIgnoreCase(getClient().getUsername())) { //Skype...
            throw new IllegalArgumentException(username + " joined the chat even though he was already in it?");
        }
    }

    public void removeUser(String username) {
        users.remove(username.toLowerCase());
    }

    public void kick(String username) throws ConnectionException {
        checkLoaded();
        Endpoints.MODIFY_MEMBER_URL
                .open(getClient(), getIdentity(), username)
                .expect(200, "While kicking user")
                .delete();
    }

    public void leave() throws ConnectionException {
        kick(getClient().getUsername());
    }

    @Override
    public String getJoinUrl() throws ConnectionException {
        checkLoaded();
        if (isOptionEnabled(OptionUpdateEvent.Option.JOINING_ENABLED)) {
            JsonObject data = new JsonObject();
            data.add("baseDomain", "https://join.skype.com/launch/");
            data.add("threadId", this.getIdentity());
            JsonObject object = Endpoints.GET_JOIN_URL
                    .open(getClient())
                    .as(JsonObject.class)
                    .expect(200, "While getting join URL")
                    .post(data);
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

    public void setTopic(String topic) throws ConnectionException {
        checkLoaded();
        putOption("topic", JsonValue.valueOf(topic), true);
    }

    @Override
    public BufferedImage getPicture() throws ConnectionException {
        checkLoaded();
        if (pictureUrl != null) {
            if (pictureUpdated) {
                picture = null;
                pictureUpdated = false;
            }
            if (picture == null) {
                this.picture = Endpoints
                        .custom(pictureUrl, getClient())
                        .as(BufferedImage.class)
                        .expect(200, "While fetching image")
                        .header("Authorization", Endpoints.AUTHORIZATION.provide(getClient()))
                        .get();
            }
            BufferedImage clone = new BufferedImage(picture.getWidth(), picture.getHeight(), picture.getType());
            Graphics2D g2d = clone.createGraphics();
            g2d.drawImage(picture, 0, 0, null);
            g2d.dispose();
            return clone;
        }
        return null;
    }

    @Override
    public void setImage(BufferedImage image, String imageType) throws ConnectionException, IOException {
        checkLoaded();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, imageType, baos);
        String id = Utils.uploadImage(baos.toByteArray(), Utils.ImageType.AVATAR, this);
        putOption("picture", JsonValue.valueOf(
                String.format("URL@https://api.asm.skype.com/v1/objects/%s/views/avatar_fullsize", id)), true);
    }

    @Override
    public void setImage(File image) throws ConnectionException, IOException {
        checkLoaded();
        byte[] data = Files.readAllBytes(image.toPath());
        String id = Utils.uploadImage(data, Utils.ImageType.AVATAR, this);
        putOption("picture", JsonValue.valueOf(
                String.format("URL@https://api.asm.skype.com/v1/objects/%s/views/avatar_fullsize", id)), true);
    }

    @Override
    public boolean isOptionEnabled(OptionUpdateEvent.Option option) {
        checkLoaded();
        return this.enabledOptions.contains(option);
    }

    @Override
    public void setOptionEnabled(OptionUpdateEvent.Option option, boolean enabled) throws ConnectionException {
        checkLoaded();
        putOption(option.getId(), JsonValue.valueOf(enabled), true);
        updateOption(option, enabled);
    }

    @Override
    public void add(Contact contact) throws ConnectionException {
        checkLoaded();
        Endpoints.ADD_MEMBER_URL
                .open(getClient(), getIdentity(), contact.getUsername())
                .expect(200, "While adding user to group")
                .put(new JsonObject().add("role", "User"));
    }

    public void updateTopic(String topic) {
        this.topic = topic;
    }

    public void updatePicture(String picture) {
        this.pictureUrl = picture;
        pictureUpdated = true;
    }

    public void updateOption(OptionUpdateEvent.Option option, boolean enabled) {
        if (enabled) enabledOptions.add(option);
        else enabledOptions.remove(option);
    }
}

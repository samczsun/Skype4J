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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.Factory;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.participants.BotImpl;
import com.samczsun.skype4j.internal.participants.ParticipantImpl;
import com.samczsun.skype4j.internal.participants.UserImpl;
import com.samczsun.skype4j.internal.Utils;
import com.samczsun.skype4j.internal.participants.info.ContactImpl;
import com.samczsun.skype4j.participants.Participant;
import com.samczsun.skype4j.participants.info.Contact;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class ChatGroup extends ChatImpl implements GroupChat {

    private String topic;
    private String pictureUrl;
    private boolean pictureUpdated;
    private BufferedImage picture;

    private Set<Option> enabledOptions = new HashSet<>();

    private List<String> bannedIds = new ArrayList<>();
    private List<String> whitelistedIds = new ArrayList<>();

    public ChatGroup(SkypeImpl skype, String identity) throws ConnectionException, ChatNotFoundException {
        super(skype, identity);
    }

    public void load() throws ConnectionException, ChatNotFoundException {
        JsonObject object = Endpoints.CHAT_INFO_URL
                .open(getClient(), getIdentity())
                .as(JsonObject.class)
                .on(404, (connection) -> {
                    throw new ChatNotFoundException();
                })
                .expect(200, "While loading users")
                .get();

        JsonObject props = object.get("properties").asObject();
        for (Option option : GroupChat.Option.values()) {
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
            String id = element.asObject().get("id").asString();
            String role = element.asObject().get("role").asString();
            ParticipantImpl user = Factory.createParticipant(getClient(), this, id);
            users.put(id.toLowerCase(), user);
            if (role.equalsIgnoreCase("admin")) {
                user.updateRole(Participant.Role.ADMIN);
            } else {
                user.updateRole(Participant.Role.USER);
            }
        }

        Map<String, UserImpl> toLoad = new HashMap<>();

        for (ParticipantImpl participant : this.users.values()) {
            if (participant instanceof UserImpl) {
                if (getClient().getContact(participant.getId()) != null) {
                    ((UserImpl) participant).setInfo(getClient().getContact(participant.getId()));
                } else {
                    toLoad.put(((UserImpl) participant).getUsername(), (UserImpl) participant);
                }
            } else if (participant instanceof BotImpl) {
                ((BotImpl) participant).setInfo(getClient().getOrLoadBotInfo(participant.getId()));
            }
        }

        while (!toLoad.isEmpty()) {
            Map<String, UserImpl> localToLoad = new HashMap<>();
            Iterator<Map.Entry<String, UserImpl>> it = toLoad.entrySet().iterator();
            while (localToLoad.size() < 100 && !toLoad.isEmpty()) {
                Map.Entry<String, UserImpl> ent = it.next();
                localToLoad.put(ent.getKey(), ent.getValue());
                it.remove();
            }

            JsonArray usernames = new JsonArray();
            localToLoad.keySet().forEach(usernames::add);

            JsonArray info = Endpoints.PROFILE_INFO
                    .open(getClient())
                    .expect(200, "While getting contact info")
                    .as(JsonArray.class)
                    .post(new JsonObject()
                            .add("usernames", usernames)
                    );

            for (JsonValue jsonValue : info) {
                JsonObject data = jsonValue.asObject();

                UserImpl matching = localToLoad.get(data.get("username").asString());
                if (matching != null) {
                    matching.setInfo(ContactImpl.createContact(getClient(), matching.getUsername(), data));
                }
            }
        }
    }

    public void addUser(String username) throws ConnectionException {
        username = "8:" + username;
        if (!users.containsKey(username.toLowerCase())) {
            ParticipantImpl user = Factory.createParticipant(getClient(), this, username);
            users.put(username.toLowerCase(), user);
        } else if (!username.equalsIgnoreCase(getClient().getUsername())) { //Skype...
            throw new IllegalArgumentException(username + " joined the chat even though he was already in it?");
        }
    }

    public void removeUser(String username) {
        username = "8:" + username;
        users.remove(username.toLowerCase());
    }

    public void kick(String username) throws ConnectionException {
        Endpoints.MODIFY_MEMBER_URL
                .open(getClient(), getIdentity(), username)
                .expect(200, "While kicking user")
                .delete();
    }

    @Override
    public void ban(String id) throws ConnectionException {
        this.bannedIds.add(id);
        updateBanlist();
    }

    @Override
    public void unban(String id) throws ConnectionException {
        this.bannedIds.remove(id);
        updateBanlist();
    }

    @Override
    public List<String> getBannedIds() {
        return Collections.unmodifiableList(this.bannedIds);
    }

    @Override
    public void whitelist(String id) throws ConnectionException {
        this.whitelistedIds.add(id);
        updateWhitelist();
    }

    @Override
    public void unwhitelist(String id) throws ConnectionException {
        this.whitelistedIds.remove(id);
        updateWhitelist();
    }

    @Override
    public List<String> getWhitelistedIds() {
        return Collections.unmodifiableList(this.whitelistedIds);
    }

    public void leave() throws ConnectionException {
        kick(getClient().getUsername());
    }

    @Override
    public String getJoinUrl() throws ConnectionException {
        if (isOptionEnabled(GroupChat.Option.JOINING_ENABLED)) {
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
        return this.topic;
    }

    public void setTopic(String topic) throws ConnectionException {
        putOption("topic", JsonValue.valueOf(topic), true);
    }

    @Override
    public BufferedImage getPicture() throws ConnectionException {
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, imageType, baos);
        String id = Utils.uploadImage(baos.toByteArray(), Utils.ImageType.AVATAR, this);
        putOption("picture", JsonValue.valueOf(
                String.format("URL@https://api.asm.skype.com/v1/objects/%s/views/avatar_fullsize", id)), true);
    }

    @Override
    public void setImage(File image) throws ConnectionException, IOException {
        byte[] data = Files.readAllBytes(image.toPath());
        String id = Utils.uploadImage(data, Utils.ImageType.AVATAR, this);
        putOption("picture", JsonValue.valueOf(
                String.format("URL@https://api.asm.skype.com/v1/objects/%s/views/avatar_fullsize", id)), true);
    }

    @Override
    public boolean isOptionEnabled(Option option) {
        return this.enabledOptions.contains(option);
    }

    @Override
    public void setOptionEnabled(Option option, boolean enabled) throws ConnectionException {
        putOption(option.getId(), JsonValue.valueOf(enabled), true);
        updateOption(option, enabled);
    }

    @Override
    public void add(Contact contact) throws ConnectionException {
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

    public void updateOption(Option option, boolean enabled) {
        if (enabled) enabledOptions.add(option);
        else enabledOptions.remove(option);
    }

    private void updateBanlist() throws ConnectionException {
        JsonValue bannedUserList = JsonValue.valueOf(bannedIds.stream().collect(Collectors.joining(",")));
        putOption("banneduserlist", bannedUserList, true);
    }

    private void updateWhitelist() throws ConnectionException {
        JsonValue whitelistedUserList = JsonValue.valueOf(whitelistedIds.stream().collect(Collectors.joining(",")));
        putOption("alloweduserlist", whitelistedUserList, true);
    }
}

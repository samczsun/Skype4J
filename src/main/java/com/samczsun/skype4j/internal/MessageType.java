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

package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.chat.messages.ReceivedMessage;
import com.samczsun.skype4j.chat.messages.SentMessage;
import com.samczsun.skype4j.chat.objects.ReceivedFile;
import com.samczsun.skype4j.events.UnsupportedEvent;
import com.samczsun.skype4j.events.chat.ChatJoinedEvent;
import com.samczsun.skype4j.events.chat.ChatQuitEvent;
import com.samczsun.skype4j.events.chat.call.CallReceivedEvent;
import com.samczsun.skype4j.events.chat.message.MessageDeletedEvent;
import com.samczsun.skype4j.events.chat.message.MessageEditedEvent;
import com.samczsun.skype4j.events.chat.message.MessageReceivedEvent;
import com.samczsun.skype4j.events.chat.message.MessageSentEvent;
import com.samczsun.skype4j.events.chat.message.SmsReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.ContactReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.FileReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.FlikReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.LocationReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.MultiContactReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.PictureReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.TypingReceivedEvent;
import com.samczsun.skype4j.events.chat.user.LegacyMemberAddedEvent;
import com.samczsun.skype4j.events.chat.user.LegacyMemberUpgradedEvent;
import com.samczsun.skype4j.events.chat.user.MultiUserAddEvent;
import com.samczsun.skype4j.events.chat.user.UserAddEvent;
import com.samczsun.skype4j.events.chat.user.UserRemoveEvent;
import com.samczsun.skype4j.events.chat.user.action.OptionUpdateEvent;
import com.samczsun.skype4j.events.chat.user.action.PictureUpdateEvent;
import com.samczsun.skype4j.events.chat.user.action.RoleUpdateEvent;
import com.samczsun.skype4j.events.chat.user.action.TopicUpdateEvent;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.NoSuchUserException;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.IMoji;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.lang.en.Moji;
import com.samczsun.skype4j.internal.chat.ChatGroup;
import com.samczsun.skype4j.internal.chat.ChatImpl;
import com.samczsun.skype4j.internal.chat.messages.ChatMessageImpl;
import com.samczsun.skype4j.internal.chat.objects.ReceivedFileImpl;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User;
import com.samczsun.skype4j.user.User.Role;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.unbescape.html.HtmlEscape;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum MessageType {
    UNKNOWN("Unknown") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
            throw new IllegalArgumentException("Unknown type!");
        }
    },
    TEXT_INTERNAL("TextInternalShouldNotBeUsedOutside") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws SkypeException, IOException {
            String content = Utils.getString(resource, "content");
            ChatImpl chat = getChat(resource, skype);
            UserImpl user = getSender(resource, chat);

            if (content == null) {
                final String clientId = resource.get("skypeeditedid").asString();
                ChatMessage m = user.getMessageById(clientId);
                if (m != null) {
                    MessageDeletedEvent event = new MessageDeletedEvent(m);
                    skype.getEventDispatcher().callEvent(event);
                    ((ChatMessageImpl) m).edit0(null);
                }
                return;
            }

            content = stripMetadata(content);

            if (resource.get("clientmessageid") != null) { // New message
                String clientId = resource.get("clientmessageid").asString();
                String id = resource.get("id").asString();
                if (resource.get("content") == null) {
                    throw new IllegalArgumentException("Null content? " + resource);
                }
                if (user != null) {
                    ChatMessage m = ChatMessageImpl.createMessage(chat, user, id, clientId, System.currentTimeMillis(),
                            Message.fromHtml(content), skype);
                    chat.onMessage(m);
                    if (m instanceof ReceivedMessage) {
                        MessageReceivedEvent event = new MessageReceivedEvent((ReceivedMessage) m);
                        skype.getEventDispatcher().callEvent(event);
                    } else {
                        MessageSentEvent event = new MessageSentEvent((SentMessage) m);
                        skype.getEventDispatcher().callEvent(event);
                    }
                } else {
                    throw new IllegalArgumentException("Null sender? " + resource);
                }
            } else if (resource.get("skypeeditedid") != null) {
                final String clientId = resource.get("skypeeditedid").asString();
                ChatMessage m = user.getMessageById(clientId);
                if (m != null) {
                    MessageEditedEvent evnt = new MessageEditedEvent(m, content);
                    skype.getEventDispatcher().callEvent(evnt);
                    ((ChatMessageImpl) m).edit0(Message.fromHtml(content));
                }
            } else {
                throw new IllegalArgumentException("Message had no id - hacking by user or skype changed their api");
            }
        }
    },
    TEXT("Text") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws SkypeException, IOException {
            MessageType.TEXT_INTERNAL.handle(skype, resource);
        }
    },
    RICH_TEXT("RichText") {
        @Override
        public void handle(final SkypeImpl skype, JsonObject resource) throws SkypeException, IOException {
            String content = Utils.getString(resource, "content");
            Validate.notNull(content, "Null content");
            Matcher matcher = URIOBJECT.matcher(content);
            if (matcher.find()) {
                String type = matcher.group(1).split("\\.")[0];
                if (type.equalsIgnoreCase("credit")) {
                    throw new IllegalArgumentException(
                            "This event needs an implementation. Please open a ticket with the stacktrace");
                } else if (type.equalsIgnoreCase("poll")) {
                    throw new IllegalArgumentException(
                            "This event needs an implementation. Please open a ticket with the stacktrace");
                } else {
                    throw new IllegalArgumentException("Unknown rich type " + type);
                }
            } else {
                MessageType.TEXT_INTERNAL.handle(skype, resource);
            }
        }
    },
    RICH_TEXT_CONTACTS("RichText/Contacts") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String content = Utils.getString(resource, "content");
            String chatId = Utils.getString(resource, "conversationLink");
            String author = getAuthor(resource);
            Validate.notNull(content, "Null content");
            Validate.notNull(chatId, "Null chat");
            Validate.notNull(author, "Null author");
            String username = getUsername(author);
            Validate.notNull(username, "Null username");
            Chat chat = getChat(chatId, skype);
            Validate.notNull(chat, "Null chatobj");
            User initiator = chat.getUser(username);
            Validate.notNull(initiator, "Null initiator");

            List<Contact> contacts = new ArrayList<>();
            Matcher matcher = CONTACT_PATTERN.matcher(content);
            while (matcher.find()) {
                String contactUsername;
                if (matcher.group(2).equals("s")) contactUsername = matcher.group(6);
                else contactUsername = matcher.group(4);
                Contact contact = skype.getOrLoadContact(contactUsername);
                if (contact != null) {
                    contacts.add(contact);
                } else {
                    skype.getLogger().log(Level.SEVERE, "Null contact (" + contactUsername + ")");
                }
            }

            if (contacts.size() > 0) {
                ContactReceivedEvent event = contacts.size() == 1 ? new ContactReceivedEvent(chat, initiator,
                        contacts.get(0)) : new MultiContactReceivedEvent(chat, initiator, contacts);
                skype.getEventDispatcher().callEvent(event);
            } else {
                throw new IllegalArgumentException("No contacts sent");
            }
        }
    },
    RICH_TEXT_FILES("RichText/Files") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String content = Utils.getString(resource, "content");
            String chatId = Utils.getString(resource, "conversationLink");
            String author = getAuthor(resource);
            Validate.notNull(content, "Null content");
            Validate.notNull(chatId, "Null chat");
            Validate.notNull(author, "Null author");
            String username = getUsername(author);
            Validate.notNull(username, "Null username");
            Chat chat = getChat(chatId, skype);
            Validate.notNull(chat, "Null chatobj");
            User initiator = chat.getUser(username);
            Validate.notNull(initiator, "Null initiator");
            Document doc = Parser.xmlParser().parseInput(content, "");

            List<ReceivedFile> receivedFiles = doc
                    .getElementsByTag("file")
                    .stream()
                    .map(fe -> new ReceivedFileImpl(fe.text(), Long.parseLong(fe.attr("size")),
                            Long.parseLong(fe.attr("tid"))))
                    .collect(Collectors.toList());

            FileReceivedEvent event = new FileReceivedEvent(chat, initiator,
                    Collections.unmodifiableList(receivedFiles));
            skype.getEventDispatcher().callEvent(event);
        }
    },
    RICH_TEXT_SMS("RichText/Sms") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException { //Implemented via fullExperience
            String content = resource.get("content").asString();
            String from = resource.get("from").asString();
            String url = resource.get("conversationLink").asString();
            Chat c = getChat(url, skype);
            User u = getUser(from, c);
            Matcher m = SMS_PATTERN.matcher(content);
            if (m.find()) {
                String message = m.group(1);
                ChatMessage chatmessage = ChatMessageImpl.createMessage(c, u, null, null, System.currentTimeMillis(),
                        Message.fromHtml(message), skype); //No clientmessageid?
                SmsReceivedEvent event = new SmsReceivedEvent((ReceivedMessage) chatmessage);
                skype.getEventDispatcher().callEvent(event);
            } else {
                throw new IllegalArgumentException("Sms event did not conform to format expected");
            }
        }
    },
    RICH_TEXT_LOCATION("RichText/Location") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException { //Implemented via fullExperience
            String content = resource.get("content").asString();
            Chat c = getChat(resource.get("conversationLink").asString(), skype);
            User u = getUser(resource.get("from").asString(), c);
            Matcher m = LOCATION_PATTERN.matcher(content);
            if (m.find()) {
                String location = m.group(1);
                String text = m.group(2);
                LocationReceivedEvent event = new LocationReceivedEvent(c, u,
                        new LocationReceivedEvent.LocationInfo(location, text));
                skype.getEventDispatcher().callEvent(event);
            } else {
                throw conformError("Location");
            }
        }
    },
    RICH_TEXT_URI_OBJECT("RichText/UriObject") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String from = resource.get("from").asString();
            String url = resource.get("conversationLink").asString();
            ChatImpl c = (ChatImpl) getChat(url, skype);
            User u = getUser(from, c);
            String content = resource.get("content").asString();
            Document doc = Parser.xmlParser().parseInput(content, "");
            if (doc.getElementsByTag("meta").size() == 0) {
                throw new IllegalArgumentException("No meta? " + resource);
            }
            Element meta = doc.getElementsByTag("meta").get(0);
            if (meta.attr("type").equalsIgnoreCase("photo")) {
                String blob = doc.getElementsByTag("a").get(0).attr("href");
                Matcher matcher = BLOBID.matcher(blob);
                if (!matcher.find()) {
                    throw new IllegalArgumentException("Blob ID has changed?");
                }
                blob = matcher.group(1);
                JsonObject obj = Endpoints.IMG_STATUS
                        .open(skype, blob, "imgpsh_fullsize")
                        .as(JsonObject.class)
                        .cookies(skype.getCookies())
                        .expect(200, "While getting URI object")
                        .get();
                Endpoints.EndpointConnection<JsonObject> econn = Endpoints
                        .custom(obj.get("status_location").asString(), skype)
                        .as(JsonObject.class)
                        .expect(200, "While getting URI object")
                        .header("Cookie", Endpoints.COOKIE.provide(skype));
                while (true) {
                    obj = econn.get();
                    if (obj.get("content_state").asString().equalsIgnoreCase("ready")) {
                        break;
                    }
                }
                BufferedImage img = Endpoints
                        .custom(obj.get("view_location").asString(), skype)
                        .header("Cookie", Endpoints.COOKIE.provide(skype))
                        .as(BufferedImage.class)
                        .expect(200, "While getting URI object")
                        .get();
                skype.getEventDispatcher().callEvent(new PictureReceivedEvent(c, u, meta.attr("originalName"), img));
            } else {
                throw new IllegalArgumentException("Unknown meta type " + meta.attr("type"));
            }
        }
    },
    RICH_TEXT_MEDIA_FLIK_MSG("RichText/Media_FlikMsg") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, IOException, ChatNotFoundException {
            ChatImpl chat = getChat(resource, skype);
            UserImpl sender = getSender(resource, chat);
            String content = Utils.getString(resource, "content");
            Validate.notNull(content, "Null content");
            Matcher matcher = URIOBJECT_URI.matcher(content);
            Validate.isTrue(matcher.find(), "Bad match");
            String id = (id = matcher.group(1)).substring(id.lastIndexOf('/') + 1, id.length());
            IMoji flik = Moji.getById(id);
            Validate.notNull(flik, "No such flik");
            skype.getEventDispatcher().callEvent(new FlikReceivedEvent(chat, sender, flik));
        }
    },
    EVENT_SKYPE_VIDEO_MESSAGE("Event/SkypeVideoMessage") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, IOException, ChatNotFoundException {
//            ChatImpl chat = getChat(resource, skype);
//            UserImpl sender = getSender(resource, chat);
//            String content = Utils.getString(resource, "content");
//            if (content == null) throw new IllegalArgumentException("Null content");
//            Matcher matcher = VIDEOMESSAGE.matcher(content);
//            if (!matcher.find()) throw new IllegalArgumentException("Videomessage conformity");
//            String sid = matcher.group(1);
//            if (sender == null) {
//                throw new IllegalArgumentException("Null user");
//            }
//            String location = "https://vm.skype.com/users/8:" + skype.getUsername() + "/video_mails/" + sid;
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
            throw new IllegalArgumentException("This event needs implementation");
        }
    },
    THREAD_ACTIVITY_ADD_MEMBER("ThreadActivity/AddMember") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            ChatImpl chat = getChat(resource, skype);
            UserImpl initiator = null;
            try {
                initiator = getInitiator(resource, chat);
            } catch (NoSuchUserException ignored) {
            }
            List<User> usersAdded = new ArrayList<>();
            boolean addedSelf = false;
            Matcher matcher = SINGLE_TARGET.matcher(resource.get("content").asString());
            while (matcher.find()) {
                String username = getUsername(matcher.group(1));
                chat.addUser(username);
                usersAdded.add(chat.getUser(username));
                if (username.equalsIgnoreCase(skype.getUsername())) {
                    addedSelf = true;
                }
            }

            if (initiator == null) {
                initiator = getInitiator(resource, chat);
            }

            UserAddEvent event = null;
            if (usersAdded.size() == 0) {
                throw new IllegalArgumentException("No targets");
            } else if (usersAdded.size() == 1) {
                event = new UserAddEvent(usersAdded.get(0), initiator);
            } else if (usersAdded.size() > 1) {
                event = new MultiUserAddEvent(usersAdded, initiator);
            }
            skype.getEventDispatcher().callEvent(event);

            if (addedSelf) {
                ChatJoinedEvent cje = new ChatJoinedEvent(chat, initiator);
                skype.getEventDispatcher().callEvent(cje);
            }
        }
    },
    THREAD_ACTIVITY_DELETE_MEMBER("ThreadActivity/DeleteMember") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            ChatImpl chat = getChat(resource, skype);
            UserImpl initiator = getInitiator(resource, chat);
            List<User> usersRemoved = new ArrayList<>();
            Matcher matcher = SINGLE_TARGET.matcher(resource.get("content").asString());
            boolean removedSelf = false;
            while (matcher.find()) {
                String username = getUsername(matcher.group(1));
                usersRemoved.add(chat.getUser(username));
                chat.removeUser(username);
                if (username.equalsIgnoreCase(skype.getUsername())) {
                    removedSelf = true;
                }
            }

            if (usersRemoved.size() == 0) {
                throw new IllegalArgumentException("No targets");
            } else if (usersRemoved.size() == 1) {
                UserRemoveEvent event = new UserRemoveEvent(usersRemoved.get(0), initiator);
                skype.getEventDispatcher().callEvent(event);
            } else if (usersRemoved.size() > 1) {
                throw new IllegalArgumentException("More than one user removed?");
            }

            if (removedSelf) {
                ChatQuitEvent event = new ChatQuitEvent(chat, initiator);
                skype.getEventDispatcher().callEvent(event);
            }
        }
    },
    THREAD_ACTIVITY_ROLE_UPDATE("ThreadActivity/RoleUpdate") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            ChatImpl chat = getChat(resource, skype);
            UserImpl initiator = getInitiator(resource, chat);
            String content = resource.get("content").asString();
            Matcher timeMatcher = EVENTTIME_PATTERN.matcher(content);
            Matcher roleMatcher = ROLE_UPDATE_PATTERN.matcher(content);
            if (timeMatcher.find() && roleMatcher.find()) {
                long time = Long.parseLong(timeMatcher.group(1));
                User target = chat.getUser(roleMatcher.group(1).substring(2));
                Role role = Role.getByName(roleMatcher.group(2));
                RoleUpdateEvent event = new RoleUpdateEvent(initiator, time, target, role);
                skype.getEventDispatcher().callEvent(event);
            } else {
                throw conformError("RoleUpdate");
            }
        }
    },
    THREAD_ACTIVITY_TOPIC_UPDATE("ThreadActivity/TopicUpdate") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String content = resource.get("content").asString();
            ChatImpl chat = getChat(resource, skype);
            UserImpl initiator = getInitiator(resource, chat);
            Matcher timeMatcher = EVENTTIME_PATTERN.matcher(content);
            Matcher valueMatcher = VALUE_PATTERN.matcher(content);
            if (timeMatcher.find() && valueMatcher.find()) {
                long time = Long.parseLong(timeMatcher.group(1));
                String topic = valueMatcher.groupCount() > 0 ? HtmlEscape.unescapeHtml(valueMatcher.group(1)) : "";
                TopicUpdateEvent event = new TopicUpdateEvent(initiator, time, ((ChatGroup) chat).getTopic(), topic);
                skype.getEventDispatcher().callEvent(event);
                ((ChatGroup) chat).updateTopic(topic);
            } else {
                throw conformError("TopicUpdate");
            }
        }
    },
    THREAD_ACTIVITY_PICTURE_UPDATE("ThreadActivity/PictureUpdate") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String content = resource.get("content").asString();
            ChatImpl chat = getChat(resource, skype);
            UserImpl initiator = getInitiator(resource, chat);
            Matcher timeMatcher = EVENTTIME_PATTERN.matcher(content);
            Matcher valueMatcher = VALUE_PATTERN.matcher(content);
            if (timeMatcher.find() && valueMatcher.find()) {
                long time = Long.parseLong(timeMatcher.group(1));
                String picurl = valueMatcher.group(1).substring(4);
                PictureUpdateEvent event = new PictureUpdateEvent(initiator, time, picurl);
                skype.getEventDispatcher().callEvent(event);
                ((ChatGroup) chat).updatePicture(picurl);
            } else {
                throw conformError("PictureUpdate");
            }
        }
    },
    THREAD_ACTIVITY_HISTORY_DISCLOSED_UPDATE("ThreadActivity/HistoryDisclosedUpdate") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String content = resource.get("content").asString();
            ChatImpl chat = getChat(resource, skype);
            UserImpl initiator = getInitiator(resource, chat);
            Matcher timeMatcher = EVENTTIME_PATTERN.matcher(content);
            Matcher valueMatcher = VALUE_PATTERN.matcher(content);
            if (timeMatcher.find() && valueMatcher.find()) {
                long time = Long.parseLong(timeMatcher.group(1));
                boolean enabled = Boolean.parseBoolean(valueMatcher.group(1));
                OptionUpdateEvent event = new OptionUpdateEvent(initiator, time,
                        OptionUpdateEvent.Option.HISTORY_DISCLOSED, enabled);
                skype.getEventDispatcher().callEvent(event);
                ((ChatGroup) chat).updateOption(OptionUpdateEvent.Option.HISTORY_DISCLOSED, enabled);
            } else {
                throw conformError("HistoryDisclosedUpdate");
            }
        }
    },
    THREAD_ACTIVITY_JOINING_ENABLED_UPDATE("ThreadActivity/JoiningEnabledUpdate") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String content = resource.get("content").asString();
            ChatImpl chat = getChat(resource, skype);
            UserImpl initiator = getInitiator(resource, chat);
            Matcher timeMatcher = EVENTTIME_PATTERN.matcher(content);
            Matcher valueMatcher = VALUE_PATTERN.matcher(content);
            if (timeMatcher.find() && valueMatcher.find()) {
                long time = Long.parseLong(timeMatcher.group(1));
                boolean enabled = Boolean.parseBoolean(valueMatcher.group(1));
                OptionUpdateEvent event = new OptionUpdateEvent(initiator, time,
                        OptionUpdateEvent.Option.JOINING_ENABLED, enabled);
                skype.getEventDispatcher().callEvent(event);
                ((ChatGroup) chat).updateOption(OptionUpdateEvent.Option.JOINING_ENABLED, enabled);
            } else {
                throw conformError("JoiningEnabledUpdate");
            }
        }
    },
    THREAD_ACTIVITY_LEGACY_MEMBER_ADDED("ThreadActivity/LegacyMemberAdded") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, IOException, ChatNotFoundException {
            String content = Utils.getString(resource, "content");
            String chatId = Utils.getString(resource, "conversationLink");
            if (content == null) {
                throw new IllegalArgumentException("Null content");
            }
            if (chatId == null) {
                throw new IllegalArgumentException("Null chat");
            }
            Matcher matcher = SINGLE_TARGET.matcher(content);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Target conformity");
            }
            Chat chat = getChat(chatId, skype);
            User user = chat.getUser(matcher.group(1).substring(2));
            if (user == null) {
                throw new IllegalArgumentException("Null user");
            }
            skype.getEventDispatcher().callEvent(new LegacyMemberAddedEvent(user));
        }
    },
    THREAD_ACTIVITY_LEGACY_MEMBER_UPGRADED("ThreadActivity/LegacyMemberUpgraded") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, IOException, ChatNotFoundException {
            String content = Utils.getString(resource, "content");
            String chatId = Utils.getString(resource, "conversationLink");
            if (content == null) {
                throw new IllegalArgumentException("Null content");
            }
            if (chatId == null) {
                throw new IllegalArgumentException("Null chat");
            }
            Matcher matcher = SINGLE_TARGET.matcher(content);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Target conformity");
            }
            Chat chat = getChat(chatId, skype);
            User user = chat.getUser(matcher.group(1).substring(2));
            if (user == null) {
                throw new IllegalArgumentException("Null user");
            }
            skype.getEventDispatcher().callEvent(new LegacyMemberUpgradedEvent(user));
        }
    },
    EVENT_CALL("Event/Call") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String from = resource.get("from").asString();
            String url = resource.get("conversationLink").asString();
            String content = resource.get("content").asString();

            boolean finished = content.startsWith("<ended/>") || content.startsWith("<partlist type=\"ended\"");

            ChatImpl c = getChat(url, skype);
            User u = getUser(from, c);
            CallReceivedEvent event = new CallReceivedEvent(c, u, !finished);
            skype.getEventDispatcher().callEvent(event);
        }
    },
    CONTROL_TYPING("Control/Typing") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String from = resource.get("from").asString();
            String url = resource.get("conversationLink").asString();

            Chat c = getChat(url, skype);
            User u = getUser(from, c);
            TypingReceivedEvent event = new TypingReceivedEvent(c, u, true);
            skype.getEventDispatcher().callEvent(event);
        }
    },
    CONTROL_CLEAR_TYPING("Control/ClearTyping") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            Chat c = getChat(resource.get("conversationLink").asString(), skype);
            User u = getUser(resource.get("from").asString(), c);
            TypingReceivedEvent event = new TypingReceivedEvent(c, u, false);
            skype.getEventDispatcher().callEvent(event);
        }
    },
    CONTROL_LIVE_STATE("Control/LiveState") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) { //No plans to implement sound API as of yet
//            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
//            skype.getLogger().log(Level.SEVERE, name() + " is in need of implementation! Please open a ticket with the following JSON data");
//            skype.getLogger().log(Level.SEVERE, resource.toString());
        }
    };

    private static final Map<String, Pattern> METADATA = new HashMap<>();

    static {
        METADATA.put("text", Pattern.compile("Edited previous message: "));
        METADATA.put("edited", Pattern.compile("</?[e_m][^<>]+>"));
        METADATA.put("quoted", Pattern.compile("(<(?:/?)(?:quote|legacyquote)[^>]*>)", Pattern.CASE_INSENSITIVE));
    }

    private static final Pattern NAME = Pattern.compile("/((?:\\d+:|live:)[^/]+)");
    private static final Pattern USERNAME = Pattern.compile("^((\\d+):)+");
    private static final Pattern SINGLE_TARGET = Pattern.compile("<target>(\\d+:[^<]+)</target>");
    private static final Pattern VIDEOMESSAGE = Pattern.compile("<videomessage[^>]*?\\ssid=\"([a-f0-9]{32})\"",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern URIOBJECT = Pattern.compile("<URIObject[^>]*?\\stype=\"([^\"]+?)\"",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern URIOBJECT_URI = Pattern.compile("<URIObject[^>]*?\\suri=\"([^\"]+?)\"",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CONVERSATION = Pattern.compile("/(\\d+:[^?]*)");
    private static final Pattern INITIATOR = Pattern.compile("<initiator>(\\d+:.+)</initiator>");

    private static final Map<String, MessageType> byValue = new HashMap<>();
    private static final Pattern USER_PATTERN = Pattern.compile("8:(.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern STRIP_EDIT_PATTERN = Pattern.compile("</?[e_m][^<>]+>", Pattern.CASE_INSENSITIVE);
    private static final Pattern STRIP_QUOTE_PATTERN = Pattern.compile("(<(?:/?)(?:quote|legacyquote)[^>]*>)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern STRIP_EMOTICON_PATTERN = Pattern.compile("(<(?:/?)(?:ss)[^>]*>)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CONTACT_PATTERN = Pattern.compile(
            "(<c t=\"([^\"]+?)\"( p=\"([^\"]+?)\")?( s=\"([^\"]+?)\")?( f=\"([^\"]+?)\")?/>)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SMS_PATTERN = Pattern.compile("<sms alt=\"([^\"]+?)\">", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOCATION_PATTERN = Pattern.compile(
            "<a[^>]+href=\"https://www.bing.com/maps([^\"]+)\"[^>]*>([^<]*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVENTTIME_PATTERN = Pattern.compile("<eventtime>(\\d+)</eventtime>",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern VALUE_PATTERN = Pattern.compile("(?:<value>(.+)</value>|<value />)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern ROLE_UPDATE_PATTERN = Pattern.compile(
            "<target><id>(\\d+:.+)</id><role>(.+)</role></target>", Pattern.CASE_INSENSITIVE);
    private static final Pattern BLOBID = Pattern.compile("(0-[a-z]+-d[0-9]-[a-z0-9]{32})");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public abstract void handle(SkypeImpl skype, JsonObject resource) throws SkypeException, IOException;

    static {
        for (MessageType type : values()) {
            byValue.put(type.getValue(), type);
        }
    }

    public static MessageType getByName(String messageType) {
        return byValue.getOrDefault(messageType, MessageType.UNKNOWN);
    }

    public ChatImpl getChat(JsonObject resource, SkypeImpl skype) throws ConnectionException, IOException, ChatNotFoundException {
        String chatId = Utils.getString(resource, "conversationLink");
        if (chatId == null) {
            throw new IllegalArgumentException("Null chat");
        }
        return getChat(chatId, skype);
    }

    public UserImpl getSender(JsonObject resource, ChatImpl chat) {
        String author = getAuthor(resource);
        String username = getUsername(author);
        return chat.getUser(username);
    }

    public static ChatImpl getChat(String url, SkypeImpl skype) throws ConnectionException, ChatNotFoundException, IOException {
        Matcher m = CONVERSATION.matcher(url);
        if (m.find()) {
            return skype.getOrLoadChat(m.group(1));
        }
        throw conformError("Chat URL");
    }

    public static User getUser(String url, Chat c) {
        Matcher m = USER_PATTERN.matcher(url);
        if (m.find()) {
            return c.getUser(m.group(1));
        }
        throw conformError("User");
    }

    public static String getAuthor(JsonObject resource) {
        String from = Utils.getString(resource, "from");
        if (from == null) throw new IllegalArgumentException("Null from");
        Matcher matcher = NAME.matcher(from);
        return matcher.find() ? matcher.group(1) : from;
    }

    private static String getUsername(String author) {
        Matcher matcher = USERNAME.matcher(author);
        return matcher.find() ? matcher.replaceAll("") : author;
    }

    private static UserImpl getInitiator(JsonObject resource, ChatImpl chat) {
        String content = Utils.getString(resource, "content");
        if (content == null) throw new IllegalArgumentException("Null content");
        Matcher matcher = INITIATOR.matcher(content);
        if (matcher.find()) { //TODO Joining an open chat breaks this
            UserImpl user = chat.getUser(getUsername(matcher.group(1)));
            if (user != null) {
                return user;
            }
            throw new NoSuchUserException();
        }
        throw new IllegalArgumentException("Malformatted content");
    }


    private static IllegalArgumentException conformError(String object) {
        return new IllegalArgumentException(String.format("%s did not conform to format expected", object));
    }

    public static String stripMetadata(String content) {
        for (Pattern pattern : METADATA.values()) {
            Matcher m = pattern.matcher(content);
            content = m.find() ? m.replaceAll("") : content;
        }
        return content;
    }
}

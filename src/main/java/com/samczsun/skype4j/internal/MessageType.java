/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
 *
 * This file is part of Skype4J.
 *
 * Skype4J is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Skype4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Skype4J.
 * If not, see http://www.gnu.org/licenses/.
 */

package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.StreamUtils;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.chat.messages.ReceivedMessage;
import com.samczsun.skype4j.chat.objects.ReceivedFile;
import com.samczsun.skype4j.events.UnsupportedEvent;
import com.samczsun.skype4j.events.chat.ChatJoinedEvent;
import com.samczsun.skype4j.events.chat.call.CallReceivedEvent;
import com.samczsun.skype4j.events.chat.message.MessageEditedByOtherEvent;
import com.samczsun.skype4j.events.chat.message.MessageEditedEvent;
import com.samczsun.skype4j.events.chat.message.MessageReceivedEvent;
import com.samczsun.skype4j.events.chat.message.SmsReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.*;
import com.samczsun.skype4j.events.chat.user.MultiUserAddEvent;
import com.samczsun.skype4j.events.chat.user.UserAddEvent;
import com.samczsun.skype4j.events.chat.user.UserRemoveEvent;
import com.samczsun.skype4j.events.chat.user.action.OptionUpdateEvent;
import com.samczsun.skype4j.events.chat.user.action.PictureUpdateEvent;
import com.samczsun.skype4j.events.chat.user.action.RoleUpdateEvent;
import com.samczsun.skype4j.events.chat.user.action.TopicUpdateEvent;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.internal.chat.ChatGroup;
import com.samczsun.skype4j.internal.chat.ChatImpl;
import com.samczsun.skype4j.internal.chat.messages.ChatMessageImpl;
import com.samczsun.skype4j.internal.chat.objects.ReceivedFileImpl;
import com.samczsun.skype4j.user.Contact;
import com.samczsun.skype4j.user.User;
import com.samczsun.skype4j.user.User.Role;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.unbescape.html.HtmlEscape;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum MessageType {
    UNKNOWN("Unknown") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            throw new IllegalArgumentException("Got an unknown tag. Please open a issue at the GitHub repo");
        }
    },
    TEXT("Text") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws SkypeException, IOException {
            MessageType.RICH_TEXT.handle(skype, resource);
        }
    },
    RICH_TEXT("RichText") {
        @Override
        public void handle(final SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            if (resource.get("clientmessageid") != null) { // New message
                String clientId = resource.get("clientmessageid").asString();
                String id = resource.get("id").asString();
                String content = resource.get("content").asString();
                String from = resource.get("from").asString();
                String url = resource.get("conversationLink").asString();
                ChatImpl c = (ChatImpl) getChat(url, skype);
                User u = getUser(from, c);
                ChatMessage m = ChatMessageImpl.createMessage(c, u, id, clientId, System.currentTimeMillis(), Message.fromHtml(stripMetadata(content)), skype);
                c.onMessage(m);
                MessageReceivedEvent event = new MessageReceivedEvent((ReceivedMessage) m);
                skype.getEventDispatcher().callEvent(event);
            } else if (resource.get("skypeeditedid") != null) { // Edited
                // message
                String url = resource.get("conversationLink").asString();
                String from = resource.get("from").asString();
                final Chat c = getChat(url, skype);
                final User u = getUser(from, c); // If not original sender, then
                // fake
                final String clientId = resource.get("skypeeditedid").asString();
                final String id = resource.get("id").asString();
                String content = resource.get("content").asString();
                content = stripMetadata(content);
                boolean faker = false;
                if (content.startsWith("Edited previous message: ")) {
                    content = content.substring("Edited previous message: ".length());
                    ChatMessage m = u.getMessageById(clientId);
                    if (m != null) {
                        MessageEditedEvent evnt = new MessageEditedEvent(m, content);
                        skype.getEventDispatcher().callEvent(evnt);
                        ((ChatMessageImpl) m).edit0(Message.fromHtml(content));
                    } else {
                        faker = true;
                    }
                } else {
                    faker = true;
                }
                if (faker) {
                    Message originalContent = null;
                    for (User user : c.getAllUsers()) {
                        if (user.getMessageById(clientId) != null) {
                            originalContent = user.getMessageById(clientId).getContent();
                        }
                    }
                    final Message finalOriginalContent = originalContent;
                    MessageEditedByOtherEvent event = new MessageEditedByOtherEvent(new ChatMessage() {
                        public String getClientId() {
                            return clientId;
                        }

                        public Message getContent() {
                            return finalOriginalContent;
                        }

                        public long getSentTime() {
                            return System.currentTimeMillis();
                        }

                        public User getSender() {
                            return u;
                        }

                        public void edit(Message newMessage) throws SkypeException {
                            throw new UnsupportedOperationException();
                        }

                        public void delete() throws SkypeException {
                            throw new UnsupportedOperationException();
                        }

                        public Chat getChat() {
                            return c;
                        }

                        @Override
                        public Skype getClient() {
                            return skype;
                        }

                        public String getId() {
                            return id;
                        }
                    }, content, u);
                    skype.getEventDispatcher().callEvent(event);
                }
            } else {
                throw new IllegalArgumentException("Message had no id - hacking by user or skype changed their api");
            }
        }
    },
    RICH_TEXT_CONTACTS("RichText/Contacts") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String from = resource.get("from").asString();
            String url = resource.get("conversationLink").asString();
            String content = resource.get("content").asString();
            Document doc = Parser.xmlParser().parseInput(content, "");
            List<Contact> contacts = new ArrayList<>();
            for (Element e : doc.getElementsByTag("c")) {
                Matcher m = CONTACT_PATTERN.matcher(e.outerHtml());
                if (m.find()) {
                    String username;
                    if (m.group(2).equals("s")) username = m.group(6);
                    else username = m.group(4);
                    contacts.add(skype.getOrLoadContact(username));
                } else {
                    throw conformError("Contact");
                }
            }
            Chat c = getChat(url, skype);
            User u = getUser(from, c);
            ContactReceivedEvent event = contacts.size() == 1 ? new ContactReceivedEvent(c, u, contacts.get(0)) : new MultiContactReceivedEvent(c, u, contacts);
            skype.getEventDispatcher().callEvent(event);
        }
    },
    RICH_TEXT_FILES("RichText/Files") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String from = resource.get("from").asString();
            String url = resource.get("conversationLink").asString();
            String content = resource.get("content").asString();
            Document doc = Parser.xmlParser().parseInput(content, "");

            List<ReceivedFile> receivedFiles = new ArrayList<>();
            for (Element fe : doc.getElementsByTag("file")) {
                receivedFiles.add(new ReceivedFileImpl(fe.text(), Long.parseLong(fe.attr("size")), Long.parseLong(fe.attr("tid"))));
            }

            Chat c = getChat(url, skype);
            User u = getUser(from, c);
            FileReceivedEvent event = new FileReceivedEvent(c, u, Collections.unmodifiableList(receivedFiles));
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
                ChatMessage chatmessage = ChatMessageImpl.createMessage(c, u, null, null, System.currentTimeMillis(), Message.fromHtml(message), skype); //No clientmessageid?
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
                LocationReceivedEvent event = new LocationReceivedEvent(c, u, new LocationReceivedEvent.LocationInfo(location, text));
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
            Element meta = doc.getElementsByTag("meta").get(0);
            if (meta.attr("type").equalsIgnoreCase("photo")) {
                String blob = doc.getElementsByTag("a").get(0).attr("href");
                blob = blob.substring(blob.indexOf('?') + 1);
                try {
                    ConnectionBuilder builder = new ConnectionBuilder();
                    builder.setUrl(String.format(PICTURE_STATUS_URL, blob));
                    builder.addHeader("Cookie", skype.getCookieString());
                    HttpURLConnection statusCon = builder.build();
                    if (statusCon.getResponseCode() == 200) {
                        JsonObject obj = JsonObject.readFrom(new InputStreamReader(statusCon.getInputStream()));
                        builder.setUrl(obj.get("status_location").asString());
                        while (true) {
                            statusCon = builder.build();
                            if (statusCon.getResponseCode() == 200) {
                                obj = JsonObject.readFrom(new InputStreamReader(statusCon.getInputStream()));
                                if (obj.get("content_state").asString().equalsIgnoreCase("ready")) {
                                    break;
                                }
                            } else {
                                throw skype.generateException("While getting URI object", statusCon);
                            }
                        }
                        builder.setUrl(obj.get("view_location").asString());
                        HttpURLConnection con = builder.build();
                        if (con.getResponseCode() == 200) {
                            ByteArrayInputStream input = StreamUtils.copy(con.getInputStream());
                            BufferedImage img = ImageIO.read(input);
                            skype.getEventDispatcher().callEvent(new PictureReceivedEvent(c, u, meta.attr("originalName"), img));
                        } else {
                            throw skype.generateException("While getting URI object", con);
                        }
                    } else {
                        throw skype.generateException("While getting URI object", statusCon);
                    }
                } catch (IOException e) {
                    throw new ConnectionException("While fetching picture", e);
                }
            } else {
                throw new IllegalArgumentException("Unknown meta type " + meta.attr("type"));
            }
        }
    },
    RICH_TEXT_MEDIA_FLIK_MSG("RichText/Media_FlikMsg") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
            throw new IllegalArgumentException("This event is in need of implementation! Please open a ticket with this stacktrace");
        }
    },
    EVENT_SKYPE_VIDEO_MESSAGE("Event/SkypeVideoMessage") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
            throw new IllegalArgumentException("This event is in need of implementation! Please open a ticket with this stacktrace");
        }
    },
    THREAD_ACTIVITY_ADD_MEMBER("ThreadActivity/AddMember") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String url = resource.get("conversationLink").asString();
            Chat c = getChat(url, skype);
            List<User> usersAdded = new ArrayList<>();
            Document xml = Jsoup.parse(resource.get("content").asString(), "", Parser.xmlParser());
            User initiator = c.getUser(xml.getElementsByTag("initiator").get(0).text());
            for (Element e : xml.getElementsByTag("target")) {
                String username = e.text().substring(2);
                ((ChatImpl) c).addUser(username);
                if (username.equals(skype.getUsername())) {
                    ChatJoinedEvent event = new ChatJoinedEvent(c);
                    skype.getEventDispatcher().callEvent(event);
                } else {
                    usersAdded.add(c.getUser(username));
                }
            }
            UserAddEvent event = null;
            if (usersAdded.size() == 1) {
                event = new UserAddEvent(usersAdded.get(0), initiator);
                skype.getEventDispatcher().callEvent(event);
            } else if (usersAdded.size() > 1) {
                event = new MultiUserAddEvent(usersAdded, initiator);
                skype.getEventDispatcher().callEvent(event);
            }
        }
    },
    THREAD_ACTIVITY_DELETE_MEMBER("ThreadActivity/DeleteMember") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String url = resource.get("conversationLink").asString();
            Chat c = getChat(url, skype);
            List<User> usersRemoved = new ArrayList<>();
            Document xml = Jsoup.parse(resource.get("content").asString(), "", Parser.xmlParser());
            User initiator = c.getUser(xml.getElementsByTag("initiator").get(0).text());
            for (Element e : xml.getElementsByTag("target")) {
                String username = e.text().substring(2);
                usersRemoved.add(c.getUser(username));
                ((ChatImpl) c).removeUser(username);
            }
            UserRemoveEvent event = null;
            if (usersRemoved.size() == 1) {
                event = new UserRemoveEvent(usersRemoved.get(0), initiator);
            } else {
                throw new IllegalArgumentException("More than one user removed?");
            }
            skype.getEventDispatcher().callEvent(event);
        }
    },
    THREAD_ACTIVITY_ROLE_UPDATE("ThreadActivity/RoleUpdate") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            String content = resource.get("content").asString();
            Chat chat = getChat(resource.get("conversationLink").asString(), skype);
            Matcher initiatorMatcher = INITIATOR_PATTERN.matcher(content);
            Matcher timeMatcher = EVENTTIME_PATTERN.matcher(content);
            Matcher roleMatcher = ROLE_UPDATE_PATTERN.matcher(content);
            if (initiatorMatcher.find() && timeMatcher.find() && roleMatcher.find()) {
                User initiator = getUser(initiatorMatcher.group(1), chat);
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
            Chat chat = getChat(resource.get("conversationLink").asString(), skype);
            Matcher initiatorMatcher = INITIATOR_PATTERN.matcher(content);
            Matcher timeMatcher = EVENTTIME_PATTERN.matcher(content);
            Matcher valueMatcher = VALUE_PATTERN.matcher(content);
            if (initiatorMatcher.find() && timeMatcher.find() && valueMatcher.find()) {
                User user = getUser(initiatorMatcher.group(1), chat);
                long time = Long.parseLong(timeMatcher.group(1));
                String topic = valueMatcher.groupCount() > 0 ? HtmlEscape.unescapeHtml(valueMatcher.group(1)) : "";
                TopicUpdateEvent event = new TopicUpdateEvent(user, time, topic);
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
            Chat chat = getChat(resource.get("conversationLink").asString(), skype);
            Matcher initiatorMatcher = INITIATOR_PATTERN.matcher(content);
            Matcher timeMatcher = EVENTTIME_PATTERN.matcher(content);
            Matcher valueMatcher = VALUE_PATTERN.matcher(content);
            if (initiatorMatcher.find() && timeMatcher.find() && valueMatcher.find()) {
                User user = getUser(initiatorMatcher.group(1), chat);
                long time = Long.parseLong(timeMatcher.group(1));
                String picurl = valueMatcher.group(1).substring(4);
                PictureUpdateEvent event = new PictureUpdateEvent(user, time, picurl);
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
            Chat chat = getChat(resource.get("conversationLink").asString(), skype);
            Matcher initiatorMatcher = INITIATOR_PATTERN.matcher(content);
            Matcher timeMatcher = EVENTTIME_PATTERN.matcher(content);
            Matcher valueMatcher = VALUE_PATTERN.matcher(content);
            if (initiatorMatcher.find() && timeMatcher.find() && valueMatcher.find()) {
                User user = getUser(initiatorMatcher.group(1), chat);
                long time = Long.parseLong(timeMatcher.group(1));
                boolean enabled = Boolean.parseBoolean(valueMatcher.group(1));
                OptionUpdateEvent event = new OptionUpdateEvent(user, time, OptionUpdateEvent.Option.HISTORY_DISCLOSED, enabled);
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
            Chat chat = getChat(resource.get("conversationLink").asString(), skype);
            Matcher initiatorMatcher = INITIATOR_PATTERN.matcher(content);
            Matcher timeMatcher = EVENTTIME_PATTERN.matcher(content);
            Matcher valueMatcher = VALUE_PATTERN.matcher(content);
            if (initiatorMatcher.find() && timeMatcher.find() && valueMatcher.find()) {
                User user = getUser(initiatorMatcher.group(1), chat);
                long time = Long.parseLong(timeMatcher.group(1));
                boolean enabled = Boolean.parseBoolean(valueMatcher.group(1));
                OptionUpdateEvent event = new OptionUpdateEvent(user, time, OptionUpdateEvent.Option.JOINING_ENABLED, enabled);
                skype.getEventDispatcher().callEvent(event);
                ((ChatGroup) chat).updateOption(OptionUpdateEvent.Option.JOINING_ENABLED, enabled);
            } else {
                throw conformError("JoiningEnabledUpdate");
            }
        }
    },
    THREAD_ACTIVITY_LEGACY_MEMBER_ADDED("ThreadActivity/LegacyMemberAdded") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
            throw new IllegalArgumentException("This event is in need of implementation! Please open a ticket with this stacktrace");
        }
    },
    THREAD_ACTIVITY_LEGACY_MEMBER_UPGRADED("ThreadActivity/LegacyMemberUpgraded") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
            throw new IllegalArgumentException("This event is in need of implementation! Please open a ticket with this stacktrace");
        }
    },
    EVENT_CALL("Event/Call") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            System.out.println(name() + " " + resource);

            String from = resource.get("from").asString();
            String url = resource.get("conversationLink").asString();
            String content = resource.get("content").asString();

            boolean finished;
            finished = content.startsWith("<ended/>") || content.startsWith("<partlist type=\"ended\"");


            ChatImpl c = (ChatImpl) getChat(url, skype);
            User u = getUser(from, c);
            CallReceivedEvent event = new CallReceivedEvent(c, u, !finished);
            skype.getEventDispatcher().callEvent(event);
        }
    },
    CONTROL_TYPING("Control/Typing") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException, IOException {
            System.out.println(name() + " " + resource);

            String from = resource.get("from").asString();
            String url = resource.get("conversationLink").asString();

            Chat c = getChat(url, skype);
            User u = getUser(from, c);
            TypingReceivedEvent event = new TypingReceivedEvent(c, u, true);
            skype.getEventDispatcher().callEvent(event);
        }
    },
    CONTROL_CLEAR_TYPING("Control/ClearTyping") {
        //YaR
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
        public void handle(SkypeImpl skype, JsonObject resource) {
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
            throw new IllegalArgumentException("This event is in need of implementation! Please open a ticket with this stacktrace");
        }
    };

    private static final Map<String, MessageType> byValue = new HashMap<>();
    private static final Pattern URL_PATTERN = Pattern.compile("conversations/(.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern USER_PATTERN = Pattern.compile("8:(.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern STRIP_EDIT_PATTERN = Pattern.compile("</?[e_m][^<>]+>", Pattern.CASE_INSENSITIVE);
    private static final Pattern STRIP_QUOTE_PATTERN = Pattern.compile("(<(?:/?)(?:quote|legacyquote)[^>]*>)", Pattern.CASE_INSENSITIVE);
    private static final Pattern STRIP_EMOTICON_PATTERN = Pattern.compile("(<(?:/?)(?:ss)[^>]*>)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONTACT_PATTERN = Pattern.compile("(<c t=\"([^\"]+?)\"( p=\"([^\"]+?)\")?( s=\"([^\"]+?)\")?( f=\"([^\"]+?)\")? */>)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SMS_PATTERN = Pattern.compile("<sms alt=\"([^\"]+?)\">", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOCATION_PATTERN = Pattern.compile("<a[^>]+href=\"https://www.bing.com/maps([^\"]+)\"[^>]*>([^<]*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern INITIATOR_PATTERN = Pattern.compile("<initiator>(\\d+:.+)</initiator>", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVENTTIME_PATTERN = Pattern.compile("<eventtime>(\\d+)</eventtime>", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALUE_PATTERN = Pattern.compile("(?:<value>(.+)</value>|<value />)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROLE_UPDATE_PATTERN = Pattern.compile("<target><id>(\\d+:.+)</id><role>(.+)</role></target>", Pattern.CASE_INSENSITIVE);

    private static final String PICTURE_URL = "https://api.asm.skype.com/v1/objects/%s/views/imgpsh_fullsize";
    private static final String PICTURE_STATUS_URL = "https://api.asm.skype.com/v1/objects/%s/views/imgpsh_fullsize/status";

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
        return byValue.get(messageType);
    }

    private static Chat getChat(String url, SkypeImpl skype) throws ConnectionException, ChatNotFoundException, IOException {
        Matcher m = URL_PATTERN.matcher(url);
        if (m.find()) {
            Chat find = skype.getChat(m.group(1));
            if (find == null) {
                find = skype.loadChat(m.group(1));
            }
            return find;
        }
        throw conformError("Chat URL");
    }

    private static User getUser(String url, Chat c) {
        Matcher m = USER_PATTERN.matcher(url);
        if (m.find()) {
            return c.getUser(m.group(1));
        }
        throw conformError("User");
    }

    private static IllegalArgumentException conformError(String object) {
        return new IllegalArgumentException(String.format("%s did not conform to format expected", object));
    }

    private static String stripMetadata(String message) {
        return STRIP_EMOTICON_PATTERN.matcher(STRIP_QUOTE_PATTERN.matcher(STRIP_EDIT_PATTERN.matcher(message).replaceAll("")).replaceAll("")).replaceAll("");
    }
}

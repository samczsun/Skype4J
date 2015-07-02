package com.samczsun.skype4j.internal.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import com.google.gson.JsonObject;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.chat.User;
import com.samczsun.skype4j.chat.User.Role;
import com.samczsun.skype4j.events.chat.ChatJoinedEvent;
import com.samczsun.skype4j.events.chat.TopicChangeEvent;
import com.samczsun.skype4j.events.chat.message.MessageReceivedEvent;
import com.samczsun.skype4j.events.chat.user.MultiUserAddEvent;
import com.samczsun.skype4j.events.chat.user.MultiUserRemoveEvent;
import com.samczsun.skype4j.events.chat.user.RoleUpdateEvent;
import com.samczsun.skype4j.events.chat.user.UserAddEvent;
import com.samczsun.skype4j.events.chat.user.UserRemoveEvent;
import com.samczsun.skype4j.exceptions.SkypeException;

public enum MessageType {
    UNKNOWN("Unknown") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {

        }
    },
    TEXT("Text") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) throws SkypeException {
            MessageType.RICH_TEXT.handle(skype, resource);
        }
    },
    RICH_TEXT("RichText") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) throws SkypeException {
            if (resource.has("clientmessageid")) { //New message
                String clientId = resource.get("clientmessageid").getAsString();
                String id = resource.get("id").getAsString();
                String content = resource.get("content").getAsString();
                String from = resource.get("from").getAsString();
                String url = resource.get("conversationLink").getAsString();
                Chat c = getChat(url, skype);
                User u = getUser(from, c);
                ChatMessage m = WebChatMessage.createMessage(c, u, id, clientId, System.currentTimeMillis(), stripMetadata(content));
                MessageReceivedEvent evnt = new MessageReceivedEvent(m);
                skype.getEventDispatcher().callEvent(evnt);
            } else if (resource.has("skypeeditedid")) { //Edited message
                String clientId = resource.get("skypeeditedid").getAsString();
                String id = resource.get("id").getAsString();
                String content = resource.get("content").getAsString();
                content = content.substring("Edited previous message: ".length());
                content = stripMetadata(content);
                String from = resource.get("from").getAsString();
                String url = resource.get("conversationLink").getAsString();
                Chat c = getChat(url, skype);
                User u = getUser(from, c);
                //TODO: Actually fire the event - there's no API in place to store chatmessages yet
                //                ChatMessage m = WebChatMessage.createMessage(c, c.getUser(from), id, clientId, System.currentTimeMillis(), resource.get("content").getAsString());
                //                MessageEditedEvent evnt = new MessageEditedEvent(m);
                //                skype.getEventDispatcher().callEvent(evnt);
            } else {
                throw new SkypeException("Had no id");
            }
        }
    },
    RICH_TEXT_CONTACTS("RichText/Contacts") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    RICH_TEXT_FILES("RichText/Files") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    RICH_TEXT_SMS("RichText/Sms") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    RICH_TEXT_LOCATION("RichText/Location") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    RICH_TEXT_URI_OBJECT("RichText/UriObject") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    RICH_TEXT_MEDIA_FLIK_MSG("RichText/Media_FlikMsg") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    EVENT_SKYPE_VIDEO_MESSAGE("Event/SkypeVideoMessage") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    THREAD_ACTIVITY_ADD_MEMBER("ThreadActivity/AddMember") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
            String url = resource.get("conversationLink").getAsString();
            Chat c = getChat(url, skype);
            List<User> usersAdded = new ArrayList<>();
            Document xml = Jsoup.parse(resource.get("content").getAsString(), "", Parser.xmlParser());
            User initiator = c.getUser(xml.getElementsByTag("initiator").get(0).text());
            for (Element e : xml.getElementsByTag("target")) {
                String username = e.text().substring(2);
                if (username.equals(skype.getUsername())) {
                    ChatJoinedEvent event = new ChatJoinedEvent(c);
                    skype.getEventDispatcher().callEvent(event);
                } else {
                    usersAdded.add(c.getUser(username));
                }
                ((WebChat) c).addUser(username);
            }
            UserAddEvent event = null;
            if (usersAdded.size() == 1) {
                event = new UserAddEvent(usersAdded.get(0), initiator);
            } else {
                event = new MultiUserAddEvent(usersAdded, initiator);
            }
            skype.getEventDispatcher().callEvent(event);
        }
    },
    THREAD_ACTIVITY_DELETE_MEMBER("ThreadActivity/DeleteMember") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
            String url = resource.get("conversationLink").getAsString();
            Chat c = getChat(url, skype);
            List<User> usersRemoved = new ArrayList<>();
            Document xml = Jsoup.parse(resource.get("content").getAsString(), "", Parser.xmlParser());
            User initiator = c.getUser(xml.getElementsByTag("initiator").get(0).text());
            for (Element e : xml.getElementsByTag("target")) {
                String username = e.text().substring(2);
                usersRemoved.add(c.getUser(username));
                ((WebChat) c).removeUser(username);
            }
            UserRemoveEvent event = null;
            if (usersRemoved.size() == 1) {
                event = new UserRemoveEvent(usersRemoved.get(0), initiator);
            } else {
                event = new MultiUserRemoveEvent(usersRemoved, initiator);
            }
            skype.getEventDispatcher().callEvent(event);
        }
    },
    THREAD_ACTIVITY_ROLE_UPDATE("ThreadActivity/RoleUpdate") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
            String url = resource.get("conversationLink").getAsString();
            Chat c = getChat(url, skype);
            Document xml = Jsoup.parse(resource.get("content").getAsString(), "", Parser.xmlParser());
            User target = c.getUser(xml.getElementsByTag("id").get(0).text().substring(2));
            Role role = Role.getByName(xml.getElementsByTag("role").get(0).text());
            target.setRole(role);
            RoleUpdateEvent e = new RoleUpdateEvent(target);
            skype.getEventDispatcher().callEvent(e);
        }
    },
    THREAD_ACTIVITY_TOPIC_UPDATE("ThreadActivity/TopicUpdate") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
            String url = resource.get("conversationLink").getAsString();
            Chat c = getChat(url, skype);
            Document xml = Jsoup.parse(resource.get("content").getAsString(), "", Parser.xmlParser());
            if (xml.getElementsByTag("value").size() > 0) {
                ((WebChatGroup) c).updateTopic(StringEscapeUtils.unescapeHtml4(xml.getElementsByTag("value").get(0).text()));
            } else {
                ((WebChatGroup) c).updateTopic("");
            }
            TopicChangeEvent e = new TopicChangeEvent(c);
            skype.getEventDispatcher().callEvent(e);
        }
    },
    THREAD_ACTIVITY_PICTURE_UPDATE("ThreadActivity/PictureUpdate") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    THREAD_ACTIVITY_HISTORY_DISCLOSED_UPDATE("ThreadActivity/HistoryDisclosedUpdate") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    THREAD_ACTIVITY_JOINING_ENABLED_UPDATE("ThreadActivity/JoiningEnabledUpdate") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    THREAD_ACTIVITY_LEGACY_MEMBER_ADDED("ThreadActivity/LegacyMemberAdded") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    THREAD_ACTIVITY_LEGACY_MEMBER_UPGRADED("ThreadActivity/LegacyMemberUpgraded") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    EVENT_CALL("Event/Call") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    CONTROL_TYPING("Control/Typing") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    CONTROL_CLEAR_TYPING("Control/ClearTyping") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    },
    CONTROL_LIVE_STATE("Control/LiveState") {
        @Override
        public void handle(WebSkype skype, JsonObject resource) {
        }
    };

    private static final Map<String, MessageType> byValue = new HashMap<>();
    private static final Pattern URL_PATTERN = Pattern.compile("conversations\\/(.*)");
    private static final Pattern USER_PATTERN = Pattern.compile("8:(.*)");
    private static final Pattern STRIP_EDIT_PATTERN = Pattern.compile("<\\/?[e_m][^<>]+>");
    private static final Pattern STRIP_QUOTE_PATTERN = Pattern.compile("(<(?:\\/?)(?:quote|legacyquote)[^>]*>)", Pattern.CASE_INSENSITIVE);

    private String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public abstract void handle(WebSkype skype, JsonObject resource) throws SkypeException;

    static {
        for (MessageType type : values()) {
            byValue.put(type.getValue(), type);
        }
    }

    public static MessageType getByName(String messageType) {
        return byValue.containsKey(messageType) ? byValue.get(messageType) : UNKNOWN;
    }

    private static Chat getChat(String url, WebSkype skype) {
        Matcher m = URL_PATTERN.matcher(url);
        if (m.find()) {
            return skype.getChat(m.group(1));
        }
        return null;
    }

    private static User getUser(String url, Chat c) {
        Matcher m = USER_PATTERN.matcher(url);
        if (m.find()) {
            return c.getUser(m.group(1));
        }
        return null;
    }

    private static String stripMetadata(String message) {
        return STRIP_QUOTE_PATTERN.matcher(STRIP_EDIT_PATTERN.matcher(message).replaceAll("")).replaceAll("");
    }
}

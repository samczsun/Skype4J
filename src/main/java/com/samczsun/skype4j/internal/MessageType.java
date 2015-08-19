package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.ConnectionBuilder;
import com.samczsun.skype4j.StreamUtils;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.ChatMessage;
import com.samczsun.skype4j.events.UnsupportedEvent;
import com.samczsun.skype4j.events.chat.ChatJoinedEvent;
import com.samczsun.skype4j.events.chat.TopicChangeEvent;
import com.samczsun.skype4j.events.chat.message.MessageEditedByOtherEvent;
import com.samczsun.skype4j.events.chat.message.MessageEditedEvent;
import com.samczsun.skype4j.events.chat.message.MessageReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.ContactReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.TypingReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.CallReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.PictureReceivedEvent;
import com.samczsun.skype4j.events.chat.user.MultiUserAddEvent;
import com.samczsun.skype4j.events.chat.user.RoleUpdateEvent;
import com.samczsun.skype4j.events.chat.user.UserAddEvent;
import com.samczsun.skype4j.events.chat.user.UserRemoveEvent;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.user.User;
import com.samczsun.skype4j.user.User.Role;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum MessageType {
    UNKNOWN("Unknown") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            throw new IllegalArgumentException("Somehow got an unknown tag. Please open a issue at the GitHub repo");
        }
    },
    TEXT("Text") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws SkypeException {
            MessageType.RICH_TEXT.handle(skype, resource);
        }
    },
    RICH_TEXT("RichText") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException {
            if (resource.get("clientmessageid") != null) { // New message
                String clientId = resource.get("clientmessageid").asString();
                String id = resource.get("id").asString();
                String content = resource.get("content").asString();
                String from = resource.get("from").asString();
                String url = resource.get("conversationLink").asString();
                ChatImpl c = (ChatImpl) getChat(url, skype);
                User u = getUser(from, c);
                ChatMessage m = ChatMessageImpl.createMessage(c, u, id, clientId, System.currentTimeMillis(), Message.fromHtml(stripMetadata(content)));
                c.onMessage(m);
                MessageReceivedEvent event = new MessageReceivedEvent(m);
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
                        ((ChatMessageImpl) m).setContent(Message.fromHtml(content));
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

                        public long getTime() {
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
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException 
		{
            String from = resource.get("from").asString();
            String url = resource.get("conversationLink").asString();
            String content = resource.get("content").asString();
            Document doc = Parser.xmlParser().parseInput(content, "");
            Element elem = doc.getElementsByTag("c").first();
            Matcher m = CONTACT_PATTERN.matcher(elem.outerHtml());
            m.find();
            String username;
            if (m.group(2).equals("s")) {
                username = m.group(6);
            } else {
                username = m.group(4);
            }
            ChatImpl c = (ChatImpl) getChat(url, skype);
            User u = getUser(from, c);
            ContactReceivedEvent event = new ContactReceivedEvent(c, u, skype.getOrLoadContact(username));
            skype.getEventDispatcher().callEvent(event);
        }
    },
    RICH_TEXT_FILES("RichText/Files") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource)
        {
            System.out.println(name() + " " + resource);
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
        }
    },
    RICH_TEXT_SMS("RichText/Sms") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            System.out.println(name() + " " + resource);
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
        }
    },
    RICH_TEXT_LOCATION("RichText/Location") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            System.out.println(name() + " " + resource);
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
        }
    },
    RICH_TEXT_URI_OBJECT("RichText/UriObject") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException {
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
                        JsonObject obj = JsonObject.readFrom(StreamUtils.readFully(statusCon.getInputStream()));
                        builder.setUrl(obj.get("status_location").asString());
                        while (true) {
                            statusCon = builder.build();
                            if (statusCon.getResponseCode() == 200) {
                                obj = JsonObject.readFrom(StreamUtils.readFully(statusCon.getInputStream()));
                                if (obj.get("content_state").asString().equalsIgnoreCase("ready")) {
                                    break;
                                }
                            } else {
                                throw skype.generateException(statusCon);
                            }
                        }
                        builder.setUrl(obj.get("view_location").asString());
                        HttpURLConnection con = builder.build();
                        if (con.getResponseCode() == 200) {
                            ByteArrayInputStream input = StreamUtils.copy(con.getInputStream());
                            BufferedImage img = ImageIO.read(input);
                            skype.getEventDispatcher().callEvent(new PictureReceivedEvent(c, u, meta.attr("originalName"), img));
                        } else {
                            throw skype.generateException(con);
                        }
                    } else {
                        throw skype.generateException(statusCon);
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
            System.out.println(name() + " " + resource);
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
        }
    },
    EVENT_SKYPE_VIDEO_MESSAGE("Event/SkypeVideoMessage") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            System.out.println(name() + " " + resource);
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
        }
    },
    THREAD_ACTIVITY_ADD_MEMBER("ThreadActivity/AddMember") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws SkypeException {
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
        public void handle(SkypeImpl skype, JsonObject resource) throws SkypeException {
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
                throw new SkypeException("More than one user removed?");
            }
            skype.getEventDispatcher().callEvent(event);
        }
    },
    THREAD_ACTIVITY_ROLE_UPDATE("ThreadActivity/RoleUpdate") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws SkypeException {
            String url = resource.get("conversationLink").asString();
            Chat c = getChat(url, skype);
            Document xml = Jsoup.parse(resource.get("content").asString(), "", Parser.xmlParser());
            User target = c.getUser(xml.getElementsByTag("id").get(0).text().substring(2));
            Role role = Role.getByName(xml.getElementsByTag("role").get(0).text());
            target.setRole(role);
            RoleUpdateEvent e = new RoleUpdateEvent(target);
            skype.getEventDispatcher().callEvent(e);
        }
    },
    THREAD_ACTIVITY_TOPIC_UPDATE("ThreadActivity/TopicUpdate") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException {
            String url = resource.get("conversationLink").asString();
            Chat c = getChat(url, skype);
            Document xml = Jsoup.parse(resource.get("content").asString(), "", Parser.xmlParser());
            if (xml.getElementsByTag("value").size() > 0) {
                ((ChatGroup) c).updateTopic(StringEscapeUtils.unescapeHtml4(xml.getElementsByTag("value").get(0).text()));
            } else {
                ((ChatGroup) c).updateTopic("");
            }
            TopicChangeEvent e = new TopicChangeEvent(c);
            skype.getEventDispatcher().callEvent(e);
        }
    },
    THREAD_ACTIVITY_PICTURE_UPDATE("ThreadActivity/PictureUpdate") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            System.out.println(name() + " " + resource);
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
        }
    },
    THREAD_ACTIVITY_HISTORY_DISCLOSED_UPDATE("ThreadActivity/HistoryDisclosedUpdate") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            System.out.println(name() + " " + resource);
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
        }
    },
    THREAD_ACTIVITY_JOINING_ENABLED_UPDATE("ThreadActivity/JoiningEnabledUpdate") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            System.out.println(name() + " " + resource);
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
        }
    },
    THREAD_ACTIVITY_LEGACY_MEMBER_ADDED("ThreadActivity/LegacyMemberAdded") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            System.out.println(name() + " " + resource);
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
        }
    },
    THREAD_ACTIVITY_LEGACY_MEMBER_UPGRADED("ThreadActivity/LegacyMemberUpgraded") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            System.out.println(name() + " " + resource);
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
        }
    },
    EVENT_CALL("Event/Call") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException
		{
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
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException
		{
            System.out.println(name() + " " + resource);

            String from = resource.get("from").asString();
            String url = resource.get("conversationLink").asString();

            ChatImpl c = (ChatImpl) getChat(url, skype);
            User u = getUser(from, c);
            TypingReceivedEvent event = new TypingReceivedEvent(c, u, true); //, skype.getOrLoadContact(username));
			skype.getEventDispatcher().callEvent(event);
		}
    },
    CONTROL_CLEAR_TYPING("Control/ClearTyping") {
        @Override
		//YaR
        public void handle(SkypeImpl skype, JsonObject resource) throws ConnectionException, ChatNotFoundException 
		{
            String from = resource.get("from").asString();
            String url = resource.get("conversationLink").asString();

            ChatImpl c = (ChatImpl) getChat(url, skype);
            User u = getUser(from, c);
            TypingReceivedEvent event = new TypingReceivedEvent(c, u, false); //, skype.getOrLoadContact(username));
			skype.getEventDispatcher().callEvent(event);
		}
    },
    CONTROL_LIVE_STATE("Control/LiveState") {
        @Override
        public void handle(SkypeImpl skype, JsonObject resource) {
            System.out.println(name() + " " + resource);
            skype.getEventDispatcher().callEvent(new UnsupportedEvent(name(), resource.toString()));
        }
    };

    private static final Map<String, MessageType> byValue = new HashMap<>();
    private static final Pattern URL_PATTERN = Pattern.compile("conversations/(.*)");
    private static final Pattern USER_PATTERN = Pattern.compile("8:(.*)");
    private static final Pattern STRIP_EDIT_PATTERN = Pattern.compile("</?[e_m][^<>]+>");
    private static final Pattern STRIP_QUOTE_PATTERN = Pattern.compile("(<(?:/?)(?:quote|legacyquote)[^>]*>)", Pattern.CASE_INSENSITIVE);
    private static final Pattern STRIP_EMOTICON_PATTERN = Pattern.compile("(<(?:/?)(?:ss)[^>]*>)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONTACT_PATTERN = Pattern.compile("(<c t=\"([^\"]+?)\"( p=\"([^\"]+?)\")?( s=\"([^\"]+?)\")?( f=\"([^\"]+?)\")? */>)");

    private static final String PICTURE_URL = "https://api.asm.skype.com/v1/objects/%s/views/imgpsh_fullsize";
    private static final String PICTURE_STATUS_URL = "https://api.asm.skype.com/v1/objects/%s/views/imgpsh_fullsize/status";

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public abstract void handle(SkypeImpl skype, JsonObject resource) throws SkypeException;

    static {
        for (MessageType type : values()) {
            byValue.put(type.getValue(), type);
        }
    }

    public static MessageType getByName(String messageType) {
        return byValue.get(messageType);
    }

    private static Chat getChat(String url, SkypeImpl skype) throws ConnectionException, ChatNotFoundException {
        Matcher m = URL_PATTERN.matcher(url);
        if (m.find()) {
            Chat find = skype.getChat(m.group(1));
            if (find == null) {
                find = skype.loadChat(m.group(1));
            }
            return find;
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
        return STRIP_EMOTICON_PATTERN.matcher(STRIP_QUOTE_PATTERN.matcher(STRIP_EDIT_PATTERN.matcher(message).replaceAll("")).replaceAll("")).replaceAll("");
    }
}

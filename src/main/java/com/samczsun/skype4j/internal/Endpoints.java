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

package com.samczsun.skype4j.internal;

import com.eclipsesource.json.JsonValue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Endpoints {
    public static final Provider<String> AUTHORIZATION = new Provider<String>() {
        public String provide(SkypeImpl skype) {
            return "skype_token " + skype.getSkypeToken();
        }
    };
    public static final Provider<String> COOKIE = new Provider<String>() {
        public String provide(SkypeImpl skype) {
            return skype.getSkypeToken();
        }
    };
    public static final Endpoints ACCEPT_CONTACT_REQUEST = new Endpoints("https://api.skype.com/users/self/contacts/auth-request/%s/accept").skypetoken();
    public static final Endpoints GET_JOIN_URL = new Endpoints("https://api.scheduler.skype.com/threads").skypetoken();
    public static final Endpoints CHAT_INFO_URL = new Endpoints("https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/?view=msnp24Equivalent").cloud().regtoken();
    public static final Endpoints CONVERSATION_PROPERTY_SELF = new Endpoints("https://%sclient-s.gateway.messenger.live.com/v1/users/ME/conversations/%s/properties?name=%s").cloud().regtoken();
    public static final Endpoints SEND_MESSAGE_URL = new Endpoints("https://%sclient-s.gateway.messenger.live.com/v1/users/ME/conversations/%s/messages").cloud().regtoken();
    public static final Endpoints MODIFY_MEMBER_URL = new Endpoints("https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/members/8:%s").cloud().regtoken();
    public static final Endpoints CONVERSATION_PROPERTY_GLOBAL = new Endpoints("https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/properties?name=%s").cloud().regtoken();
    public static final Endpoints ADD_MEMBER_URL = new Endpoints("https://client-s.gateway.messenger.live.com/v1/threads/%s/members/8:%s").regtoken();
    public static final Endpoints LOGIN_URL = new Endpoints("https://login.skype.com/login?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com");
    public static final Endpoints PING_URL = new Endpoints("https://web.skype.com/api/v1/session-ping").skypetoken();
    public static final Endpoints TOKEN_AUTH_URL = new Endpoints("https://api.asm.skype.com/v1/skypetokenauth");
    public static final Endpoints LOGOUT_URL = new Endpoints("https://login.skype.com/logout?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com&intsrc=client-_-webapp-_-production-_-go-signin");
    public static final Endpoints ENDPOINTS_URL = new Endpoints("https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints");
    public static final Endpoints AUTH_REQUESTS_URL = new Endpoints("https://api.skype.com/users/self/contacts/auth-request").skypetoken();
    public static final Endpoints TROUTER_URL = new Endpoints("https://go.trouter.io/v2/a");
    public static final Endpoints POLICIES_URL = new Endpoints("https://prod.tpc.skype.com/v1/policies").skypetoken();
    public static final Endpoints REGISTRATIONS = new Endpoints("https://prod.registrar.skype.com/v2/registrations").skypetoken();
    public static final Endpoints THREAD_URL = new Endpoints("https://%sclient-s.gateway.messenger.live.com/v1/threads").cloud().regtoken();
    public static final Endpoints SUBSCRIPTIONS_URL = new Endpoints("https://%sclient-s.gateway.messenger.live.com/v1/users/ME/endpoints/SELF/subscriptions").cloud().regtoken();
    public static final Endpoints MESSAGINGSERVICE_URL = new Endpoints("https://%sclient-s.gateway.messenger.live.com/v1/users/ME/endpoints/%s/presenceDocs/messagingService").cloud().regtoken();
    public static final Endpoints POLL = new Endpoints("https://%sclient-s.gateway.messenger.live.com/v1/users/ME/endpoints/SELF/subscriptions/0/poll").cloud().regtoken();
    public static final Endpoints NEW_GUEST = new Endpoints("https://join.skype.com/api/v1/users/guests");
    public static final Endpoints LEAVE_GUEST = new Endpoints("https://join.skype.com/guests/leave?threadId=%s");
    public static final Endpoints ACTIVE = new Endpoints("https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints/%s/active").regtoken();
    public static final Endpoints LOAD_CHATS = new Endpoints("https://client-s.gateway.messenger.live.com/v1/users/ME/conversations?startTime=%s&pageSize=%s&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread").regtoken();
    public static final Endpoints LOAD_MESSAGES = new Endpoints("https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/%s/messages?startTime=0&pageSize=%s&view=msnp24Equivalent|supportsMessageProperties&targetType=Passport|Skype|Lync|Thread").regtoken();
    public static final Endpoints OBJECTS = new Endpoints("https://api.asm.skype.com/v1/objects").defaultHeader("Authorization", AUTHORIZATION);
    public static final Endpoints UPLOAD_IMAGE = new Endpoints("https://api.asm.skype.com/v1/objects/%s/content/%s").defaultHeader("Authorization", AUTHORIZATION);
    public static final Endpoints IMG_STATUS = new Endpoints("https://api.asm.skype.com/v1/objects/%s/views/%s/status").defaultHeader("Cookie", COOKIE);
    public static final Endpoints FETCH_IMAGE = new Endpoints("https://api.asm.skype.com/v1/objects/%s/views/%s").defaultHeader("Authorization", AUTHORIZATION);
    public static final Endpoints VISIBILITY = new Endpoints("https://%sclient-s.gateway.messenger.live.com/v1/users/ME/presenceDocs/messagingService").cloud().regtoken();
    public static final Endpoints SEARCH_SKYPE_DIRECTORY = new Endpoints("https://api.skype.com/search/users/any?keyWord=%s&contactTypes[]=skype").skypetoken();
    public static final Endpoints GET_ALL_CONTACTS = new Endpoints("https://contacts.skype.com/contacts/v1/users/%s/contacts?$filter=type%%20eq%%20%%27skype%%27%%20or%%20type%%20eq%%20%%27msn%%27%%20or%%20type%%20eq%%20%%27pstn%%27%%20or%%20type%%20eq%%20%%27agent%%27&reason=%s").skypetoken();
    public static final Endpoints GET_CONTACT_BY_ID = new Endpoints("https://contacts.skype.com/contacts/v1/users/%s/contacts?$filter=id%%20eq%%20%%27%s%%27&reason=default").skypetoken();
    public static final Endpoints BLOCK_CONTACT = new Endpoints("https://api.skype.com/users/self/contacts/%s/block").skypetoken();
    public static final Endpoints UNBLOCK_CONTACT = new Endpoints("https://api.skype.com/users/self/contacts/%s/unblock").skypetoken();
    public static final Endpoints AUTHORIZE_CONTACT = new Endpoints("https://api.skype.com/users/self/contacts/auth-request/%s/accept").skypetoken();
    public static final Endpoints UNAUTHORIZE_CONTACT = new Endpoints("https://client-s.gateway.messenger.live.com/v1/users/ME/contacts/8:%s").regtoken();
    public static final Endpoints DECLINE_CONTACT_REQUEST = new Endpoints("https://api.skype.com/users/self/contacts/auth-request/%s/decline").skypetoken();
    public static final Endpoints UNAUTHORIZE_CONTACT_SELF = new Endpoints("https://api.skype.com/users/self/contacts/%s").skypetoken();
    public static final Endpoints AUTHORIZATION_REQUEST = new Endpoints("https://api.skype.com/users/self/contacts/auth-request/%s").skypetoken();
    public static final Endpoints CONTACT_INFO = new Endpoints("https://api.skype.com/users/self/contacts/profiles").skypetoken();

    private boolean requiresCloud;
    private boolean requiresRegToken;
    private boolean requiresSkypeToken;

    private Map<String, Provider<String>> providers = new HashMap<>();

    private String url;

    public static EndpointConnection custom(String url, SkypeImpl skype, String... args) {
        return new EndpointConnection(new Endpoints(url), skype, args);
    }

    public String url() {
        return this.url;
    }

    private Endpoints(String url) {
        this.url = url;
    }

    public EndpointConnection open(SkypeImpl skype, Object... args) {
        return new EndpointConnection(this, skype, args);
    }

    private Endpoints cloud() {
        this.requiresCloud = true;
        return this;
    }

    private Endpoints regtoken() {
        this.requiresRegToken = true;
        return this;
    }

    private Endpoints skypetoken() {
        this.requiresSkypeToken = true;
        return this;
    }

    private Endpoints defaultHeader(String key, Provider<String> val) {
        this.providers.put(key, val);
        return this;
    }

    public static class EndpointConnection {
        private Endpoints endpoint;
        private SkypeImpl skype;
        private Object[] args;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> cookies = new HashMap<>();
        private URL url;

        private EndpointConnection(Endpoints endpoint, SkypeImpl skype, Object[] args) {
            this.endpoint = endpoint;
            this.skype = skype;
            this.args = args;
            if (endpoint.requiresRegToken) {
                header("RegistrationToken", skype.getRegistrationToken());
            }
            if (endpoint.requiresSkypeToken) {
                header("X-SkypeToken", skype.getSkypeToken());
            }
            for (Map.Entry<String, Provider<String>> provider : endpoint.providers.entrySet()) {
                header(provider.getKey(), provider.getValue().provide(skype));
            }
        }

        public EndpointConnection header(String key, String val) {
            this.headers.put(key, val);
            return this;
        }

        public EndpointConnection cookies(Map<String, String> cookies) {
            this.cookies.putAll(cookies);
            return this;
        }

        public EndpointConnection cookie(String key, String val) {
            this.cookies.put(key, val);
            return this;
        }

        public HttpURLConnection get() throws IOException {
            return connect("GET", null);
        }

        public HttpURLConnection delete() throws IOException {
            return connect("DELETE", null);
        }

        public HttpURLConnection post() throws IOException {
            return connect("POST", null);
        }

        public HttpURLConnection post(String data) throws IOException {
            return connect("POST", data);
        }

        public HttpURLConnection post(JsonValue json) throws IOException {
            return header("Content-Type", "application/json").connect("POST", json.toString());
        }

        public HttpURLConnection put() throws IOException {
            return connect("PUT", null);
        }

        public HttpURLConnection put(String data) throws IOException {
            return connect("PUT", data);
        }

        public HttpURLConnection put(JsonValue json) throws IOException {
            return header("Content-Type", "application/json").connect("PUT", json.toString());
        }

        public HttpURLConnection connect(String method, String rawData) throws IOException {
            if (!cookies.isEmpty()) {
                header("Cookie", serializeCookies(cookies));
            }
            if (this.url == null) { //todo could fail if cloud is updated?
                String surl = endpoint.url;
                if (endpoint.requiresCloud) {
                    Object[] format = new Object[args.length + 1];
                    format[0] = skype.getCloud();
                    for (int i = 1; i < format.length; i++) {
                        format[i] = args[i - 1].toString();
                    }
                    surl = String.format(surl, format);
                } else if (args.length > 0) {
                    Object[] format = new Object[args.length];
                    for (int i = 0; i < format.length; i++) {
                        format[i] = args[i].toString();
                    }
                    surl = String.format(surl, args);
                }
                this.url = new URL(surl);
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setInstanceFollowRedirects(false);
            for (Map.Entry<String, String> ent : headers.entrySet()) {
                connection.setRequestProperty(ent.getKey(), ent.getValue());
            }
            if (!method.equalsIgnoreCase("GET")) {
                connection.setDoOutput(true);
                if (rawData != null) {
                    connection.getOutputStream().write(rawData.getBytes(StandardCharsets.UTF_8));
                } else {
                    connection.getOutputStream().write(new byte[0]);
                }
            }
            return connection;
        }

        private String serializeCookies(Map<String, String> cookies) {
            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                result.append(cookie.getKey()).append("=").append(cookie.getValue()).append(";");
            }
            return result.toString();
        }
    }

    public interface Provider<T> {
        T provide(SkypeImpl skype);
    }
}

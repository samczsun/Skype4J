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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.internal.utils.Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class Endpoints {
    private static Map<Class<?>, Converter<?>> converters = new HashMap<>();

    static {
        converters.put(InputStream.class, HttpURLConnection::getInputStream);
        converters.put(HttpURLConnection.class, in -> in);
        converters.put(JsonObject.class, in -> Utils.parseJsonObject(in.getInputStream()));
        converters.put(JsonArray.class, in -> Utils.parseJsonArray(in.getInputStream()));
        converters.put(String.class, in -> StreamUtils.readFully(in.getInputStream()));
        converters.put(BufferedImage.class, in -> ImageIO.read(in.getInputStream()));
    }

    public static <T> T convert(Class<?> type, SkypeImpl skype, HttpURLConnection in) throws IOException {
        return (T) converters.get(type).convert(in);
    }

    public static final Provider<String> AUTHORIZATION = skype -> "skype_token " + skype.getSkypeToken();
    public static final Provider<String> COOKIE = skype -> "skypetoken_asm=" + skype.getSkypeToken();
    public static final Endpoints ACCEPT_CONTACT_REQUEST = new Endpoints(
            "https://api.skype.com/users/self/contacts/auth-request/%s/accept").skypetoken();
    public static final Endpoints GET_JOIN_URL = new Endpoints("https://api.scheduler.skype.com/threads").skypetoken();
    public static final Endpoints CHAT_INFO_URL = new Endpoints(
            "https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/?view=msnp24Equivalent").cloud().regtoken();
    public static final Endpoints CONVERSATION_PROPERTY_SELF = new Endpoints(
            "https://%sclient-s.gateway.messenger.live.com/v1/users/ME/conversations/%s/properties?name=%s")
            .cloud()
            .regtoken();
    public static final Endpoints SEND_MESSAGE_URL = new Endpoints(
            "https://%sclient-s.gateway.messenger.live.com/v1/users/ME/conversations/%s/messages").cloud().regtoken();
    public static final Endpoints MODIFY_MEMBER_URL = new Endpoints(
            "https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/members/8:%s").cloud().regtoken();
    public static final Endpoints CONVERSATION_PROPERTY_GLOBAL = new Endpoints(
            "https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/properties?name=%s").cloud().regtoken();
    public static final Endpoints ADD_MEMBER_URL = new Endpoints(
            "https://client-s.gateway.messenger.live.com/v1/threads/%s/members/8:%s").regtoken();
    public static final Endpoints LOGIN_URL = new Endpoints("https://api.skype.com/login/skypetoken");
    public static final Endpoints PING_URL = new Endpoints("https://web.skype.com/api/v1/session-ping").skypetoken();
    public static final Endpoints TOKEN_AUTH_URL = new Endpoints("https://api.asm.skype.com/v1/skypetokenauth");
    public static final Endpoints LOGOUT_URL = new Endpoints(
            "https://login.skype.com/logout?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com&intsrc=client-_-webapp-_-production-_-go-signin");
    public static final Endpoints ENDPOINTS_URL = new Endpoints(
            "https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints");
    public static final Endpoints AUTH_REQUESTS_URL = new Endpoints(
            "https://api.skype.com/users/self/contacts/auth-request").skypetoken();
    public static final Endpoints TROUTER_URL = new Endpoints("https://go.trouter.io/v2/a");
    public static final Endpoints POLICIES_URL = new Endpoints("https://prod.tpc.skype.com/v1/policies").skypetoken();
    public static final Endpoints REGISTRATIONS = new Endpoints(
            "https://prod.registrar.skype.com/v2/registrations").skypetoken();
    public static final Endpoints THREAD_URL = new Endpoints("https://%sclient-s.gateway.messenger.live.com/v1/threads")
            .cloud()
            .regtoken();
    public static final Endpoints SUBSCRIPTIONS_URL = new Endpoints(
            "https://%sclient-s.gateway.messenger.live.com/v1/users/ME/endpoints/SELF/subscriptions")
            .cloud()
            .regtoken();
    public static final Endpoints MESSAGINGSERVICE_URL = new Endpoints(
            "https://%sclient-s.gateway.messenger.live.com/v1/users/ME/endpoints/%s/presenceDocs/messagingService")
            .cloud()
            .regtoken();
    public static final Endpoints POLL = new Endpoints(
            "https://%sclient-s.gateway.messenger.live.com/v1/users/ME/endpoints/SELF/subscriptions/%s/poll")
            .cloud()
            .regtoken();
    public static final Endpoints NEW_GUEST = new Endpoints("https://join.skype.com/api/v1/users/guests");
    public static final Endpoints LEAVE_GUEST = new Endpoints("https://join.skype.com/guests/leave?threadId=%s");
    public static final Endpoints ACTIVE = new Endpoints(
            "https://%sclient-s.gateway.messenger.live.com/v1/users/ME/endpoints/%s/active").cloud().regtoken();
    public static final Endpoints LOAD_CHATS = new Endpoints(
            "https://client-s.gateway.messenger.live.com/v1/users/ME/conversations?startTime=%s&pageSize=%s&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread|PSTN|Agent")
            .regtoken();
    public static final Endpoints LOAD_MESSAGES = new Endpoints(
            "https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/%s/messages?startTime=0&pageSize=%s&view=msnp24Equivalent|supportsMessageProperties&targetType=Passport|Skype|Lync|Thread")
            .regtoken();
    public static final Endpoints OBJECTS = new Endpoints("https://api.asm.skype.com/v1/objects").defaultHeader(
            "Authorization", AUTHORIZATION);
    public static final Endpoints UPLOAD_IMAGE = new Endpoints(
            "https://api.asm.skype.com/v1/objects/%s/content/%s").defaultHeader("Authorization", AUTHORIZATION);
    public static final Endpoints IMG_STATUS = new Endpoints(
            "https://api.asm.skype.com/v1/objects/%s/views/%s/status").defaultHeader("Cookie", COOKIE);
    public static final Endpoints FETCH_IMAGE = new Endpoints(
            "https://api.asm.skype.com/v1/objects/%s/views/%s").defaultHeader("Authorization", AUTHORIZATION);
    public static final Endpoints VISIBILITY = new Endpoints(
            "https://%sclient-s.gateway.messenger.live.com/v1/users/ME/presenceDocs/messagingService")
            .cloud()
            .regtoken();
    public static final Endpoints SEARCH_SKYPE_DIRECTORY = new Endpoints(
            "https://api.skype.com/search/users/any?keyWord=%s&contactTypes[]=skype").skypetoken();
    public static final Endpoints GET_ALL_CONTACTS = new Endpoints(
            "https://contacts.skype.com/contacts/v1/users/%s/contacts?delta&$filter=type%%20eq%%20%%27skype%%27%%20or%%20type%%20eq%%20%%27msn%%27%%20or%%20type%%20eq%%20%%27pstn%%27%%20or%%20type%%20eq%%20%%27agent%%27%%20or%%20type%%20eq%%20%%27lync%%27&reason=%s")
            .skypetoken();
    public static final Endpoints GET_CONTACT_BY_ID = new Endpoints(
            "https://contacts.skype.com/contacts/v1/users/%s/contacts?$filter=id%%20eq%%20%%27%s%%27&reason=default").skypetoken();
    public static final Endpoints BLOCK_CONTACT = new Endpoints(
            "https://api.skype.com/users/self/contacts/%s/block").skypetoken();
    public static final Endpoints UNBLOCK_CONTACT = new Endpoints(
            "https://api.skype.com/users/self/contacts/%s/unblock").skypetoken();
    public static final Endpoints AUTHORIZE_CONTACT = new Endpoints(
            "https://api.skype.com/users/self/contacts/auth-request/%s/accept").skypetoken();
    public static final Endpoints UNAUTHORIZE_CONTACT = new Endpoints(
            "https://client-s.gateway.messenger.live.com/v1/users/ME/contacts/8:%s").regtoken();
    public static final Endpoints DECLINE_CONTACT_REQUEST = new Endpoints(
            "https://api.skype.com/users/self/contacts/auth-request/%s/decline").skypetoken();
    public static final Endpoints UNAUTHORIZE_CONTACT_SELF = new Endpoints(
            "https://api.skype.com/users/self/contacts/%s").skypetoken();
    public static final Endpoints AUTHORIZATION_REQUEST = new Endpoints(
            "https://api.skype.com/users/self/contacts/auth-request/%s").skypetoken();
    public static final Endpoints CONTACT_INFO = new Endpoints(
            "https://api.skype.com/users/self/contacts/profiles").skypetoken();
    public static final Endpoints RECONNECT_WEBSOCKET = new Endpoints(
            "https://go.trouter.io/v2/h?ccid=%s&dom=web.skype.com");
    public static final Endpoints ELIGIBILITY_CHECK = new Endpoints("https://web.skype.com/api/v2/eligibility-check").skypetoken();

    // todo implement
    // what other scopes are there?
    public static final Endpoints LANGUAGES_GET = new Endpoints("https://dev.microsofttranslator.com/api/languages?scope=text").skypetoken();

    public static final Endpoints NEW_KEY = new Endpoints("https://kes.skype.com/v2/swx/newkey").skypetoken();
    public static final Endpoints PETOKEN = new Endpoints("https://static.asm.skype.com/pes/v1/petoken").defaultHeader("Authorization", AUTHORIZATION);
    public static final Endpoints PROFILE = new Endpoints("https://api.skype.com/users/self/profile").skypetoken();

    private boolean requiresCloud;
    private boolean requiresRegToken;
    private boolean requiresSkypeToken;

    private Map<String, Provider<String>> providers = new HashMap<>();

    private String url;

    public String url() {
        return this.url;
    }

    private Endpoints(String url) {
        this.url = url;
    }

    public static EndpointConnection<HttpURLConnection> custom(String url, SkypeImpl skype, String... args) {
        if (skype.isShutdownRequested()) {
            throw new IllegalStateException("API is shut down");
        }
        return new EndpointConnection(new Endpoints(url), skype, args).as(HttpURLConnection.class);
    }

    public EndpointConnection<HttpURLConnection> open(SkypeImpl skype, Object... args) {
        if (skype.isShutdownRequested()) {
            throw new IllegalStateException("API is shut down");
        }
        return new EndpointConnection(this, skype, args).as(HttpURLConnection.class);
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

    public static class EndpointConnection<E_TYPE> {
        private Class<E_TYPE> clazz = (Class<E_TYPE>) HttpURLConnection.class;
        private Endpoints endpoint;
        private SkypeImpl skype;
        private Object[] args;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> cookies = new HashMap<>();
        private Map<Predicate<Integer>, UncheckedFunction<E_TYPE>> errors = new HashMap<>();
        private URL url;
        private String cause;
        private boolean dontConnect;
        private boolean redirect = true;

        private EndpointConnection(Endpoints endpoint, SkypeImpl skype, Object[] args) {
            this.endpoint = endpoint;
            this.skype = skype;
            this.args = args;
            header("User-Agent",
                    "Mozilla/5.0 (Windows NT 10; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.73 Safari/537.36 Skype4J/" + SkypeImpl.VERSION);
        }

        public EndpointConnection<E_TYPE> header(String key, String val) {
            this.headers.put(key, val);
            return this;
        }

        public EndpointConnection<E_TYPE> cookie(String key, String val) {
            this.cookies.put(key, val);
            return this;
        }

        public EndpointConnection<E_TYPE> cookies(Map<String, String> cookies) {
            this.cookies.putAll(cookies);
            return this;
        }

        public EndpointConnection<E_TYPE> on(int code, UncheckedFunction<E_TYPE> action) {
            return on(x -> x == code, action);
        }

        public EndpointConnection<E_TYPE> on(Predicate<Integer> check, UncheckedFunction<E_TYPE> result) {
            this.errors.put(check, result);
            return this;
        }

        public EndpointConnection<E_TYPE> expect(int code, String cause) {
            return expect(x -> x == code, cause);
        }

        public EndpointConnection<E_TYPE> expect(Predicate<Integer> check, String cause) {
            this.cause = cause;
            return on(check, (connection) -> convert(clazz, skype, connection));
        }

        public EndpointConnection<E_TYPE> noRedirects() {
            this.redirect = false;
            return this;
        }

        public <NEW_E_TYPE> EndpointConnection<NEW_E_TYPE> as(Class<NEW_E_TYPE> clazz) {
            this.clazz = (Class<E_TYPE>) clazz;
            return (EndpointConnection<NEW_E_TYPE>) this;
        }

        public EndpointConnection<E_TYPE> dontConnect() {
            this.dontConnect = true;
            return this;
        }

        public E_TYPE get() throws ConnectionException {
            return connect("GET", new byte[0]);
        }

        public E_TYPE delete() throws ConnectionException {
            return connect("DELETE", new byte[0]);
        }

        public E_TYPE post() throws ConnectionException {
            return connect("POST", new byte[0]);
        }

        public E_TYPE post(String data) throws ConnectionException {
            return connect("POST", data);
        }

        public E_TYPE post(JsonValue json) throws ConnectionException {
            return header("Content-Type", "application/json").connect("POST", json.toString());
        }

        public E_TYPE put() throws ConnectionException {
            return connect("PUT", new byte[0]);
        }

        public E_TYPE put(String data) throws ConnectionException {
            return connect("PUT", data);
        }

        public E_TYPE put(JsonValue json) throws ConnectionException {
            return header("Content-Type", "application/json").connect("PUT", json.toString());
        }

        public E_TYPE connect(String method, String data) throws ConnectionException {
            return this.connect(method, data != null ? data.getBytes(StandardCharsets.UTF_8) : new byte[0]);
        }

        public E_TYPE connect(String method, byte[] rawData) throws ConnectionException {
            if (!cookies.isEmpty()) {
                header("Cookie", serializeCookies(cookies));
            }
            if (endpoint.requiresRegToken) {
                header("RegistrationToken", skype.getRegistrationToken());
            }
            if (endpoint.requiresSkypeToken) {
                header("X-SkypeToken", skype.getSkypeToken());
            }
            if (this.redirect) {
                this.on(code -> (code >= 301 && code <= 303) || code == 307 || code == 308, connection -> {
                    skype.updateCloud(connection.getHeaderField("Location"));
                    this.url = new URL(connection.getHeaderField("Location"));
                    return this.connect(method, rawData);
                });
            }
            for (Map.Entry<String, Provider<String>> provider : endpoint.providers.entrySet()) {
                header(provider.getKey(), provider.getValue().provide(skype));
            }
            HttpURLConnection connection = null;
            try {
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
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(method);
                connection.setInstanceFollowRedirects(false);
                for (Map.Entry<String, String> ent : headers.entrySet()) {
                    connection.setRequestProperty(ent.getKey(), ent.getValue());
                }
                if (!method.equalsIgnoreCase("GET")) {
                    connection.setDoOutput(true);
                    if (rawData != null) {
                        connection.getOutputStream().write(rawData);
                    } else {
                        connection.getOutputStream().write(new byte[0]);
                    }
                }
                if (!this.dontConnect) {
                    if (connection.getHeaderField("Set-RegistrationToken") != null) {
                        skype.setRegistrationToken(connection.getHeaderField("Set-RegistrationToken"));
                    }
                    for (Map.Entry<Predicate<Integer>, UncheckedFunction<E_TYPE>> entry : errors.entrySet()) {
                        if (entry.getKey().test(connection.getResponseCode())) {
                            try {
                                return entry.getValue().apply(connection);
                            } catch (Throwable t) {
                                Utils.sneakyThrow(t);
                            }
                        }
                    }
                    throw ExceptionHandler.generateException(cause == null ? this.url.toString() : cause, connection);
                } else if (HttpURLConnection.class.isAssignableFrom(clazz)) {
                    return (E_TYPE) connection;
                } else {
                    throw new IllegalArgumentException(
                            "DontConnect requested but did not request cast to HttpURLConnection");
                }
            } catch (IOException e) {
                throw ExceptionHandler.generateException(cause, e);
            } finally {
                if (clazz != InputStream.class && clazz != HttpURLConnection.class) {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }

        private String serializeCookies(Map<String, String> cookies) {
            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                result.append(Encoder.encode(cookie.getKey())).append("=").append(Encoder.encode(cookie.getValue())).append(";");
            }
            return result.toString();
        }
    }

    public interface Provider<T> {
        T provide(SkypeImpl skype);
    }

    public interface Converter<T> {
        T convert(HttpURLConnection connection) throws IOException;
    }

    public interface UncheckedFunction<R> extends Function<HttpURLConnection, R> {
        default R apply(HttpURLConnection httpURLConnection) {
            try {
                return apply0(httpURLConnection);
            } catch (Throwable t) {
                Utils.sneakyThrow(t);
            }
            return null;
        }

        R apply0(HttpURLConnection httpURLConnection) throws Throwable;
    }
}

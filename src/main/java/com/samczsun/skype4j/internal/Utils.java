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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.internal.chat.ChatImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Utils {

    public static JsonObject parseJsonObject(InputStream inputStream) throws IOException {
        return parseJsonValue(inputStream).asObject();
    }

    public static JsonArray parseJsonArray(InputStream inputStream) throws IOException {
        return parseJsonValue(inputStream).asArray();
    }

    public static JsonValue parseJsonValue(InputStream inputStream) throws IOException {
        InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
        JsonValue jsonValue;
        try {
            jsonValue = JsonValue.readFrom(reader);
        } finally {
            reader.close();
        }
        return jsonValue;
    }

    public static String uploadImage(byte[] image,ImageType uploadType, ChatImpl chat) throws ConnectionException {
        return upload(image, uploadType, null, chat);
    }

    public static String upload(byte[] data, ImageType type, JsonObject extra, ChatImpl chat) throws ConnectionException {
        JsonObject obj = new JsonObject();
        obj.add("type", type.mime);
        obj.add("permissions", new JsonObject().add(chat.getIdentity(), new JsonArray().add("read")));
        if (extra != null) extra.forEach(m -> obj.add(m.getName(), m.getValue()));

        JsonObject response = Endpoints.OBJECTS
                .open(chat.getClient())
                .as(JsonObject.class)
                .expect(201, "While uploading data")
                .post(obj);

        String id = response.get("id").asString();

        Endpoints.UPLOAD_IMAGE
                .open(chat.getClient(), id, type.endpoint)
                .header("Content-Type", "multipart/form-data")
                .expect(201, "While uploading data")
                .connect("PUT", data);

        Endpoints.EndpointConnection<JsonObject> econn = Endpoints.IMG_STATUS
                .open(chat.getClient(), id, type.id)
                .as(JsonObject.class)
                .expect(200, "While getting upload status");
        while (true) {
            JsonObject status = econn.get();
            if (status.get("view_state").asString().equals("ready")) {
                break;
            }
        }
        return id;
    }

    public static String getString(JsonObject object, String key) {
        return object.get(key) == null ? null : object.get(key).isNull() ? null : object.get(key).asString();
    }

    public enum ImageType {
        IMGT1("pish/image", "imgpsh", "imgt1"),
        AVATAR("avatar/group", "avatar", "avatar_fullsize"), //Also has "avatar"
        FILE("sharing/file", "original", "thumbnail");

        private String mime;
        private String endpoint;
        private String id;

        ImageType(String mime, String endpoint, String id) {
            this.mime = mime;
            this.endpoint = endpoint;
            this.id = id;
        }
    }

    public static <T> Stream<T> asStream(Iterable<T> sourceIterable) {
        return asStream(sourceIterable.iterator());
    }

    public static <T> Stream<T> asStream(Iterator<T> sourceIterator) {
        return asStream(sourceIterator, false);
    }

    public static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    public static String encodeSilently(String original) {
        try {
            return URLEncoder.encode(original, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

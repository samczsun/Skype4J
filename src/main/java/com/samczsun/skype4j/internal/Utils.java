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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

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

    public static String uploadImage(BufferedImage image, String imageType, ImageType uploadType, ChatImpl chat) throws ConnectionException {
        try {
            JsonObject obj = new JsonObject();
            obj.add("type", uploadType.mime);
            obj.add("permissions", new JsonObject().add(chat.getIdentity(), new JsonArray().add("read")));
            HttpURLConnection connection = Endpoints.OBJECTS.open(chat.getClient()).post(obj);

            if (connection.getResponseCode() != 201) {
                throw ExceptionHandler.generateException("While uploading image", connection);
            }

            JsonObject response = JsonObject.readFrom(new InputStreamReader(connection.getInputStream()));
            String id = response.get("id").asString();
            connection = Endpoints.UPLOAD_IMAGE.open(chat.getClient(), id, uploadType.endpoint).header("Content-Type", "multipart/form-data").put();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, imageType, baos);
            baos.flush();
            connection.getOutputStream().write(baos.toByteArray());
            if (connection.getResponseCode() != 201) {
                throw ExceptionHandler.generateException("While uploading image", connection);
            }

            Endpoints.EndpointConnection econn = Endpoints.IMG_STATUS.open(chat.getClient(), id, uploadType.id);
            while (true) {
                HttpURLConnection conn = econn.get();
                if (conn.getResponseCode() != 200) {
                    throw ExceptionHandler.generateException("While getting image status", conn);
                }
                JsonObject status = JsonObject.readFrom(new InputStreamReader(conn.getInputStream()));
                if (status.get("view_state").asString().equals("ready")) {
                    break;
                }
            }
            return id;
        } catch (IOException e) {
            throw ExceptionHandler.generateException("While uploading image", e);
        }
    }

    public static String getString(JsonObject object, String key) {
        return object.get(key) == null ? null : object.get(key).isNull() ? null : object.get(key).asString();
    }

    public enum ImageType {
        IMGT1("pish/image", "imgpsh", "imgt1"),
        AVATAR("avatar/group", "avatar", "avatar_fullsize"); //Also has "avatar"

        private String mime;
        private String endpoint;
        private String id;

        ImageType(String mime, String endpoint, String id) {
            this.mime = mime;
            this.endpoint = endpoint;
            this.id = id;
        }
    }
}

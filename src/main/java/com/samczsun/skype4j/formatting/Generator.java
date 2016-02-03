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

package com.samczsun.skype4j.formatting;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.samczsun.skype4j.internal.StreamUtils;
import com.samczsun.skype4j.internal.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Generator {
    private static final String LANG = "en"; //Change to whatever lang you want
    private static final String VERSION = "908_1.20.0.98"; //Magic number

    public static void main(String[] args) throws Exception {
        URL url = new URL(
                "https://a.config.skype.com/config/v1/Skype/" + VERSION + "/SkypePersonalization?apikey=skype.com&id=self&callback=Skype4J");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Skype4J");
        String str = StreamUtils.readFully(connection.getInputStream());
        JsonObject object = JsonObject.readFrom(str.substring(12, str.length() - 1));
        String configloc = object.get("pes_config").asString();
        configloc = configloc.substring(0, configloc.lastIndexOf('/') + 1);
        URL config = new URL(configloc + LANG);
        connection = (HttpURLConnection) config.openConnection();
        connection.setRequestProperty("User-Agent", "Skype4J");
        JsonObject root = Utils.parseJsonObject(connection.getInputStream());
        JsonArray items = root.get("items").asArray();

        {
            Scanner in = new Scanner(
                    new File("src/main/java/com/samczsun/skype4j/formatting/lang/DefaultEmoticon.java"), "UTF-8");

            File f = new File("src/main/java/com/samczsun/skype4j/formatting/lang/" + LANG + "/Emoticon.java");
            if (!f.exists()) {
                if (!f.getParentFile().mkdirs()) throw new IllegalArgumentException("Could not create folder");
                if (!f.createNewFile()) throw new IllegalArgumentException("Could not create file");
            }
            PrintWriter pr = new PrintWriter(f, "UTF-8");

            while (in.hasNextLine()) {
                String next = in.nextLine();
                if (next.startsWith("package")) {
                    pr.println("package com.samczsun.skype4j.formatting.lang." + LANG + ";");
                } else if (next.contains("DefaultEmoticon")) {
                    pr.println(next.replace("DefaultEmoticon", "Emoticon"));
                } else if (next.trim().equals(";")) {
                    List<JsonObject> emoticons = new ArrayList<>();
                    for (JsonValue val : items) {
                        if (val.asObject().get("type").asString().equals("emoticon")) {
                            emoticons.add(val.asObject());
                        }
                    }
                    for (int i = 0; i < emoticons.size(); i++) {
                        JsonObject obj = emoticons.get(i);
                        String enumname = obj.get("id").asString().toUpperCase().replace(' ', '_');
                        String id = obj.get("id").asString();
                        String etag = obj.get("etag").asString();
                        String desc = obj.get("description").asString();
                        String shortcuts = obj.get("shortcuts").toString();
                        shortcuts = shortcuts.substring(1, shortcuts.length() - 1);
                        pr.println(String.format(
                                "    %s(\"%s\",\"%s\",\"%s\",%s)" + (i == emoticons.size() - 1 ? ";" : ","), enumname,
                                id, etag, desc, shortcuts));
                    }
                } else {
                    pr.println(next);
                }
            }
            pr.close();
        }

        {
            Scanner in = new Scanner(new File("src/main/java/com/samczsun/skype4j/formatting/lang/DefaultFlik.java"));
            File f = new File("src/main/java/com/samczsun/skype4j/formatting/lang/" + LANG + "/Moji.java");
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(f);
            PrintWriter pr = new PrintWriter(out);

            while (in.hasNextLine()) {
                String next = in.nextLine();
                if (next.startsWith("package")) {
                    pr.println("package com.samczsun.skype4j.formatting.lang." + LANG + ";");
                } else if (next.contains("DefaultFlik")) {
                    pr.println(next.replace("DefaultFlik", "Flik"));
                } else if (next.trim().equals(";")) {
                    List<JsonObject> emoticons = new ArrayList<>();
                    for (JsonValue val : items) {
                        if (val.asObject().get("type").asString().equals("flik")) {
                            emoticons.add(val.asObject());
                        }
                    }
                    Set<String> enumnames = new HashSet<>();
                    for (int i = 0; i < emoticons.size(); i++) {
                        JsonObject obj = emoticons.get(i);
                        int x = 0;
                        String enumname = obj
                                .get("description")
                                .asString()
                                .toUpperCase()
                                .replace(' ', '_')
                                .replaceAll("[^a-zA-Z]", "");
                        String orig = enumname;
                        String id = obj.get("id").asString();
                        String etag = obj.get("etag").asString();
                        String desc = obj.get("description").asString();
                        while (!enumnames.add(enumname)) {
                            enumname = orig + "_" + (++x);
                        }
                        pr.println(String.format(
                                "    %s(\"%s\", \"%s\", \"%s\")" + (i == emoticons.size() - 1 ? ";" : ","), enumname,
                                id, etag, desc));
                    }
                } else {
                    pr.println(next);
                }
            }
            pr.close();
        }
    }
}

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

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionBuilder {
    private String url;
    private Map<String, List<String>> headers = new HashMap<>();
    private String method = "GET";
    private String data;

    private boolean output;

    private volatile URL builtUrl;

    public void setUrl(String url) {
        this.url = url;
        builtUrl = null;
    }

    public void addHeader(String header, String value) {
        if (!this.headers.containsKey(header)) {
            this.headers.put(header, new ArrayList<String>());
        }
        this.headers.get(header).add(value);
    }

    public void setMethod(String method, boolean output) {
        this.method = method;
        this.output = output;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setData(JsonValue object) {
        setData(object.toString());
        addHeader("Content-Type", "application/json");
    }

    public String getData() {
        return this.data;
    }

    public HttpURLConnection build() throws IOException {
        return build(0);
    }

    public HttpURLConnection build(int timeout) throws IOException {
        if (builtUrl == null) {
            builtUrl = new URL(url);
        }
        HttpURLConnection con = (HttpURLConnection) builtUrl.openConnection();
        con.setReadTimeout(timeout);
        con.setInstanceFollowRedirects(false);
        for (Map.Entry<String, List<String>> ent : headers.entrySet()) {
            for (String value : ent.getValue()) {
                con.addRequestProperty(ent.getKey(), value);
            }
        }
        con.setRequestMethod(method);
        con.setDoOutput(output);
        if (data != null && output) {
            con.getOutputStream().write(data.getBytes(Charset.forName("UTF-8")));
        }
        return con;
    }
}

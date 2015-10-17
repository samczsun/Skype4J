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
        if (data != null) {
            con.getOutputStream().write(data.getBytes(Charset.forName("UTF-8")));
        }
        return con;
    }
}

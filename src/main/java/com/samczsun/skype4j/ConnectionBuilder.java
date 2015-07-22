package com.samczsun.skype4j;

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

    public void setUrl(String url) {
        this.url = url;
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
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
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

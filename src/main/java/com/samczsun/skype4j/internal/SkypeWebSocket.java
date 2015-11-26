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
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.internal.client.FullClient;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class SkypeWebSocket extends WebSocketClient {
    private SkypeImpl skype;
    private Thread pingThread;
    private ExecutorService singleThreaded = Executors.newSingleThreadExecutor();

    public SkypeWebSocket(SkypeImpl skype, URI uri) throws NoSuchAlgorithmException, KeyManagementException {
        super(uri, new Draft_17(), null, 2000);
        this.skype = skype;
        TrustManager[] trustAllCerts = new TrustManager[]{new TrustAllManager()};
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        this.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sc, singleThreaded));
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        (pingThread = new Thread() {
            AtomicInteger currentPing = new AtomicInteger(1);
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(55 * 1000);
                        send("5:" + currentPing.getAndIncrement() + "+::{\"name\":\"ping\"}");
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }).start();
    }

    @Override
    public void onMessage(String s) {
        if (s.startsWith("3:::")) {
            JsonObject message = JsonObject.readFrom(s.substring(4));
            JsonObject body = JsonObject.readFrom(message.get("body").asString());
            int event = body.get("evt").asInt();
            if (event == 6) {
                skype.getLogger().log(Level.SEVERE, "Unhandled websocket message '{0}'", s);
            } else if (event == 14) {
                try {
                    if (skype instanceof FullClient) {
                        ((FullClient) skype).checkForNewContactRequests();
                    }
                } catch (ConnectionException | IOException e) {
                    skype.getLogger().log(Level.SEVERE, String.format("Unhandled exception while parsing websocket message '%s'", s), e);
                }
            } else {
                skype.getLogger().log(Level.SEVERE, "Unhandled websocket message '{0}'", s);
            }

            JsonObject trouterRequest = new JsonObject();
            trouterRequest.add("ts", System.currentTimeMillis());
            trouterRequest.add("auth", true);

            JsonObject headers = new JsonObject();
            headers.add("trouter-request", trouterRequest);

            JsonObject trouterClient = new JsonObject();
            trouterClient.add("cd", 0);

            JsonObject response = new JsonObject();
            response.add("id", message.get("id").asInt());
            response.add("status", 200);
            response.add("headers", headers);
            response.add("trouter-client", trouterClient);
            response.add("body", "");
            this.send("3:::" + response.toString());
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        pingThread.stop();
        singleThreaded.shutdown();
        while (!singleThreaded.isTerminated());
    }

    @Override
    public void onError(Exception e) {
        skype.getLogger().log(Level.SEVERE, "Exception in websocket client", e);
    }

    private class TrustAllManager implements X509TrustManager {

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }
    }
}

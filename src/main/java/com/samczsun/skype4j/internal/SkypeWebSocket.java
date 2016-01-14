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
import com.samczsun.skype4j.exceptions.handler.ErrorSource;
import com.samczsun.skype4j.internal.client.FullClient;
import org.java_websocket.SSLSocketChannel2;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class SkypeWebSocket extends WebSocketClient {
    private final SkypeImpl skype;
    private final ExecutorService singleThreaded;
    private Thread pingThread;

    public SkypeWebSocket(final SkypeImpl skype, URI uri) throws NoSuchAlgorithmException, KeyManagementException {
        super(uri, new Draft_17(), null, 2000);
        this.skype = skype;
        this.singleThreaded = Executors.newSingleThreadExecutor(new SkypeThreadFactory(skype, "WSFactory"));
        TrustManager[] trustAllCerts = new TrustManager[]{new TrustAllManager()};
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        this.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sc, singleThreaded) {
            private boolean called = false;

            @Override
            public ByteChannel wrapChannel(SocketChannel channel, SelectionKey key, String host, int port) throws IOException {
                if (!called) {
                    Thread.currentThread().setName("Skype4J-WSMainThread-" + skype.getUsername());
                }
                SSLEngine e = sslcontext.createSSLEngine(host, port);
                e.setUseClientMode(true);
                ByteChannel c = new SSLSocketChannel2(channel, e, exec, key) {
                    private boolean called = false;

                    @Override
                    public int write(ByteBuffer buffer) throws IOException {
                        if (!called) {
                            Thread.currentThread().setName("Skype4J-WSWriteThread-" + skype.getUsername());
                            called = true;
                        }
                        return super.write(buffer);
                    }
                };
                return c;
            }
        });
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        (pingThread = new Thread("Skype4J-Pinger-" + skype.getUsername()) {
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
                try {
                    skype.updateContactList();
                } catch (ConnectionException e) {
                    skype.handleError(ErrorSource.UPDATING_CONTACT_LIST, e, false);
                }
            } else if (event == 14) {
                try {
                    if (skype instanceof FullClient) {
                        skype.getContactRequests(true);
                    }
                } catch (ConnectionException e) {
                    skype.getLogger().log(Level.SEVERE, String.format("Unhandled exception while parsing websocket message '%s'", s), e);
                }
            } else {
                skype.getLogger().log(Level.SEVERE, String.format("Unhandled websocket message '%s'", s));
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
        } else if (s.startsWith("5:")) {
            if (s.contains("reconnect")) {
                try {
                    skype.registerWebSocket();
                } catch (Exception e) {
                    skype.handleError(ErrorSource.REGISTERING_WEBSOCKET, e, false);
                }
            }
        } else if (s.equals("0::")) {
            try {
                this.closeBlocking();
            } catch (InterruptedException e) {
                skype.handleError(ErrorSource.CLOSING_WEBSOCKET, e, false);
            } finally {
                if (this.pingThread.isAlive()) {
                    this.pingThread.interrupt();
                }
                if (!this.singleThreaded.isTerminated()) {
                    singleThreaded.shutdown();
                    while (!singleThreaded.isTerminated()) ;
                }
            }
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        pingThread.interrupt();
        singleThreaded.shutdown();
        while (!singleThreaded.isTerminated()) ;
        if (skype.getWebSocket() == this) {
            try {
                skype.registerWebSocket();
            } catch (Exception e) {
                skype.handleError(ErrorSource.REGISTERING_WEBSOCKET, e, false);
            }
        }
    }

    @Override
    public void onError(Exception e) {
        skype.getLogger().log(Level.SEVERE, "Exception in websocket client", e);
    }

    private static class TrustAllManager implements X509TrustManager {

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }
    }
}

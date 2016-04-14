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

import com.samczsun.skype4j.exceptions.ConnectionException;
import sun.net.www.MessageHeader;
import sun.net.www.URLConnection;
import sun.net.www.http.HttpClient;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExceptionHandler {
    private static final boolean DEBUG;
    private static Field POSTER_FIELD;
    private static Field DELEGATE_FIELD;
    private static Field REQUESTS_FIELD;

    static {
        DEBUG = AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> Boolean.getBoolean("com.samczsun.skype4j.debugExceptions"));
        try {
            POSTER_FIELD = sun.net.www.protocol.http.HttpURLConnection.class.getDeclaredField("poster");
            POSTER_FIELD.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
        try {
            DELEGATE_FIELD = sun.net.www.protocol.https.HttpsURLConnectionImpl.class.getDeclaredField("delegate");
            DELEGATE_FIELD.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
        try {
            REQUESTS_FIELD = sun.net.www.protocol.http.HttpURLConnection.class.getDeclaredField("requests");
            REQUESTS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
    }

    public static ConnectionException generateException(String reason, HttpURLConnection connection) {
        try {
            if (DEBUG) {
                Object reflect = connection;
                if (reflect instanceof HttpsURLConnectionImpl && DELEGATE_FIELD != null) {
                    try {
                        reflect = DELEGATE_FIELD.get(reflect);
                    } catch (ReflectiveOperationException ignored) {
                    }
                }
                System.err.println("URL");
                System.err.println("\t" + connection.getURL());
                try {
                    MessageHeader messageHeader = (MessageHeader) REQUESTS_FIELD.get(reflect);
                    System.err.println("Request headers");
                    for (Map.Entry<String, List<String>> header : messageHeader.getHeaders(null).entrySet()) {
                        System.err.println(String.format("\t%s - %s", header.getKey(), header.getValue()));
                    }
                } catch (ReflectiveOperationException ignored) {

                }
                System.err.println("Response headers");
                for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                    System.err.println(String.format("\t%s - %s", header.getKey(), header.getValue()));
                }
                if (reflect instanceof sun.net.www.protocol.http.HttpURLConnection && POSTER_FIELD != null) {
                    try {
                        ByteArrayOutputStream poster = (ByteArrayOutputStream) POSTER_FIELD.get(reflect);
                        if (poster != null) {
                            System.err.println("Post data");
                            System.err.println("\t" + new String(poster.toByteArray()));
                        }
                    } catch (ReflectiveOperationException ignored) {
                    }
                }
            }
            return new ConnectionException(reason, connection);
        } catch (IOException e) {
            throw new RuntimeException(String.format("IOException while constructing exception (%s, %s)", reason, connection));
        } finally {
            connection.disconnect();
        }
    }

    public static ConnectionException generateException(String reason, IOException nested) {
        return new ConnectionException(reason, nested);
    }
}

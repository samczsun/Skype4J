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

import com.samczsun.skype4j.exceptions.ConnectionException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;

public class ExceptionHandler {
    private static final boolean DEBUG;

    static {
        DEBUG = AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> Boolean.getBoolean("com.samczsun.skype4j.debugExceptions"));
    }

    public static ConnectionException generateException(String reason, HttpURLConnection connection) {
        try {
            if (DEBUG) {
                connection.disconnect();
                System.err.println("URL");
                System.err.println("\t" + connection.getURL());
                System.err.println("Request headers");
                for (Map.Entry<String, List<String>> header : connection.getRequestProperties().entrySet()) {
                    System.err.println(String.format("\t%s - %s", header.getKey(), header.getValue()));
                }
                System.err.println("Response headers");
                for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                    System.err.println(String.format("\t%s - %s", header.getKey(), header.getValue()));
                }
            }
            return new ConnectionException(reason, connection.getResponseCode(), connection.getResponseMessage());
        } catch (IOException e) {
            throw new RuntimeException(String.format("IOException while constructing exception (%s, %s)", reason, connection));
        }
    }

    public static ConnectionException generateException(String reason, IOException nested) {
        return new ConnectionException(reason, nested);
    }
}

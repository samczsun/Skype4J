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

package com.samczsun.skype4j.exceptions;

import com.samczsun.skype4j.internal.ExceptionHandler;
import com.samczsun.skype4j.internal.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

/**
 * Thrown when an exception occurs while connecting to an endpoint
 */
public class ConnectionException extends SkypeException {
    private int responseCode;
    private String responseMessage;
    private String message;

    public ConnectionException(String cause, HttpURLConnection connection) throws IOException {
        super(null);
        this.responseCode = connection.getResponseCode();
        this.responseMessage = connection.getResponseMessage();
        StringBuilder messageBuilder = new StringBuilder(System.lineSeparator());
        messageBuilder.append("\t\t").append("Cause: ").append(cause).append(System.lineSeparator());
        messageBuilder.append("\t\t").append("Response: ").append(responseCode).append(" ").append(responseMessage).append(System.lineSeparator());
        InputStream readFrom;
        if (getResponseCode() == 401 || getResponseCode() == 407) {
            // http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/8u40-b25/sun/net/www/protocol/http/HttpURLConnection.java/#1634
            readFrom = new ByteArrayInputStream(ExceptionHandler.MESSAGES_FROM_JAVA.remove(connection).getBytes(StandardCharsets.UTF_8));
        } else if (getResponseCode() < 400) {
            readFrom = connection.getInputStream();
        } else {
            readFrom = connection.getErrorStream();
        }
        if (readFrom != null) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Utils.copy(readFrom, outputStream);
                String result = outputStream.toString("UTF-8");
                messageBuilder.append("\t\t").append("Begin Message ")
                        .append(System.lineSeparator())
                        .append(System.lineSeparator())
                        .append(result)
                        .append(System.lineSeparator())
                        .append(System.lineSeparator())
                        .append("\t\t").append("End message")
                        .append(System.lineSeparator());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("ReadFrom was null");
        }
        message = messageBuilder.toString();
    }

    public ConnectionException(String cause, IOException nested) {
        super(cause, nested);
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public String getResponseMessage() {
        return this.responseMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

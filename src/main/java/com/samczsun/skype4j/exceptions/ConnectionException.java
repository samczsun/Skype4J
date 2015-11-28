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

package com.samczsun.skype4j.exceptions;

import java.io.IOException;

/**
 * Thrown when an exception occurs while connecting to an endpoint
 */
public class ConnectionException extends SkypeException {
    private int responseCode;
    private String responseMessage;

    public ConnectionException(String cause, int responseCode, String responseMessage) {
        super(cause + String.format(" (%s~%s)", responseCode, responseMessage));
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
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
}

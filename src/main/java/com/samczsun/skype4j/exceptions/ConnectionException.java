package com.samczsun.skype4j.exceptions;

import java.io.IOException;

/**
 * Thrown when an exception occurs while connecting to one of Skype Web's endpoints
 *
 * @author samczsun
 */
public class ConnectionException extends NestedSkypeException {
    public ConnectionException(String cause, IOException exception) {
        super(cause, exception);
    }
}

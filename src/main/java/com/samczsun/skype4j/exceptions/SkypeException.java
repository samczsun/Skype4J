package com.samczsun.skype4j.exceptions;

/**
 * Represents any exception that may occur while using this API
 *
 * @author samczsun
 */
public class SkypeException extends Exception {
    public SkypeException() {
        super();
    }

    public SkypeException(String message) {
        super(message);
    }
}

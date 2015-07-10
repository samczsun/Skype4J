package com.samczsun.skype4j.exceptions;

/**
 * Created by Sam on 7/9/2015.
 */
public class InvalidCredentialsException extends SkypeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, String html) {
        super(message + "\n" + html);
    }
}

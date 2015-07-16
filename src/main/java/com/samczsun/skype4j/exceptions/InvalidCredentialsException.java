package com.samczsun.skype4j.exceptions;

/**
 * Thrown when invalid credentials are given to log in
 *
 * @author samczsun
 */
public class InvalidCredentialsException extends SkypeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, String html) {
        super(message + "\n" + html);
    }
}

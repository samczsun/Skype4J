package com.samczsun.skype4j.exceptions;

import java.io.IOException;

/**
 * Thrown when parsing of a response from Skype Web fails
 *
 * @author samczsun
 */
public class ParseException extends NestedSkypeException {
    public ParseException(String cause, IOException exception) {
        super(cause, exception);
    }
}

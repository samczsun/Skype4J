package com.samczsun.skype4j.exceptions;

/**
 * Represents a {@link SkypeException SkypeException} which holds another exception as the cause
 *
 * @author samczsun
 */
public class NestedSkypeException extends SkypeException {
    private final Exception reason;

    public NestedSkypeException(String cause, Exception suppressed) {
        super(cause);
        this.reason = suppressed;
    }

    public Exception getReason() {
        return this.reason;
    }
}

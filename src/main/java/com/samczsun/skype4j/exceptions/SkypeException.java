package com.samczsun.skype4j.exceptions;

public class SkypeException extends Exception {
    private static final long serialVersionUID = -7832042631619998728L;
    
    public SkypeException() {
        super();
    }
    
    public SkypeException(String message) {
        super(message);
    }
    
    public SkypeException(String message, Exception wrapped) {
        super(message, wrapped);
    }
}

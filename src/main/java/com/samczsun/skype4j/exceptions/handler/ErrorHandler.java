package com.samczsun.skype4j.exceptions.handler;

/**
 * Represents a handler which will handle errors produced by the API
 */
public abstract class ErrorHandler {
    /**
     * Handle the exception. Any exceptions thrown by this ErrorHandler will be silently swallowed and ignored
     * @param errorSource The cause of the error
     * @param error The error, may be null
     * @param shutdown Whether the API will shut down because this error is unrecoverable
     */
    public abstract void handle(ErrorSource errorSource, Throwable error, boolean shutdown);
}

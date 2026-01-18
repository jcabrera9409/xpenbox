package org.xpenbox.exception;

/**
 * Exception indicating a bad request, such as invalid input data.
 * Maps to HTTP 400 Bad Request status.
 */
public class BadRequestException extends BussinessException {

    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String resourceName, String field, Object value) {
        super(String.format("Invalid %s: %s '%s'", resourceName, field, value));
    }

    public BadRequestException(String message, Throwable ex) {
        super(message, ex);
    }
}

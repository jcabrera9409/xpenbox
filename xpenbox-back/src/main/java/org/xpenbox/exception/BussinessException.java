package org.xpenbox.exception;

import jakarta.ws.rs.core.Response;

/**
 * General business exception class.
 * Maps to HTTP 400 Bad Request by default.
 * Can be extended to include specific status codes.
 */
public class BussinessException extends RuntimeException {

    private final int statusCode;

    public BussinessException(String message) {
        super(message);
        this.statusCode = Response.Status.BAD_REQUEST.getStatusCode();
    }
    
    public BussinessException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = Response.Status.BAD_REQUEST.getStatusCode();
    }

    public BussinessException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public BussinessException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
    
}

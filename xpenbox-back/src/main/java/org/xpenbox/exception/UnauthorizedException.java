package org.xpenbox.exception;

import jakarta.ws.rs.core.Response;

/**
 * Exception thrown when a user is unauthorized to perform an action.
 * Maps to HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends BussinessException {

    public UnauthorizedException(String message) {
        super(message, Response.Status.UNAUTHORIZED.getStatusCode());
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause, Response.Status.UNAUTHORIZED.getStatusCode());
    }
}

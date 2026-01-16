package org.xpenbox.exception;

import jakarta.ws.rs.core.Response;

/**
 * Exception thrown when access to a resource is forbidden.
 * Maps to HTTP 403 Forbidden status.
 */
public class ForbiddenException extends BussinessException {
    
    public ForbiddenException(String message) {
        super(message, Response.Status.FORBIDDEN.getStatusCode());
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause, Response.Status.FORBIDDEN.getStatusCode());
    }
}

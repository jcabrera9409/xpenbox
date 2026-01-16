package org.xpenbox.exception;

import jakarta.ws.rs.core.Response;

/**
 * Exception thrown when a requested resource is not found.
 * Maps to HTTP 404 Not Found status.
 */
public class ResourceNotFoundException extends BussinessException {

    public ResourceNotFoundException(String message) {
        super(message, Response.Status.NOT_FOUND.getStatusCode());
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, Response.Status.NOT_FOUND.getStatusCode());
    }
    
}

package org.xpenbox.exception;

import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.ws.rs.core.Response;

/**
 * Exception indicating a conflict, such as a resource already existing.
 * Maps to HTTP 409 Conflict status.
 */
public class ConflictException extends BussinessException {

    public ConflictException(String message) {
        super(message, Response.Status.CONFLICT.getStatusCode());
    }
    
    public ConflictException(String resourceName, String field, Object value) {
        super(String.format("%s with %s '%s' already exists", resourceName, field, value), HttpResponseStatus.CONFLICT.code());
    }
}

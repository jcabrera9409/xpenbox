package org.xpenbox.exception;

import jakarta.ws.rs.core.Response;

/**
 * Exception indicating that the user's email address has not been verified.
 * Maps to HTTP 428 Precondition Required status.
 */
public class EmailNotVerifiedException extends BussinessException {
    public EmailNotVerifiedException(String message) {
        super(message, Response.Status.PRECONDITION_REQUIRED.getStatusCode());
    }

    public EmailNotVerifiedException(String message, Throwable cause) {
        super(message, cause, Response.Status.PRECONDITION_REQUIRED.getStatusCode());
    }
}

package org.xpenbox.exception;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.Response;

/**
 * Exception thrown when validation errors occur.
 * Contains a list of validation error messages.
 * Maps to HTTP 400 Bad Request.
 */
public class ValidationException extends BussinessException {
    
    private final List<String> validationErrors;

    public ValidationException(String message) {
        super(message, Response.Status.BAD_REQUEST.getStatusCode());
        this.validationErrors = new ArrayList<>();
    }
    
    public ValidationException(String message, List<String> validationErrors) {
        super(message, Response.Status.BAD_REQUEST.getStatusCode());
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void addValidationError(String error) {
        this.validationErrors.add(error);
    }
}

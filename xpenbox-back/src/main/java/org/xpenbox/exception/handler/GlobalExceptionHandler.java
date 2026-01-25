package org.xpenbox.exception.handler;

import org.jboss.logging.Logger;
import org.xpenbox.common.dto.APIResponseDTO;
import org.xpenbox.exception.BadRequestException;
import org.xpenbox.exception.ConflictException;
import org.xpenbox.exception.ForbiddenException;
import org.xpenbox.exception.InsufficientFoundsException;
import org.xpenbox.exception.ResourceNotFoundException;
import org.xpenbox.exception.ValidationException;

import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Global exception handler for mapping exceptions to HTTP responses.
 * Catches various exceptions and returns appropriate HTTP status codes and messages.
 * Logs exceptions for monitoring and debugging.
 */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {
    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    /**
     * Maps exceptions to HTTP responses.
     * @param exception The exception to be handled
     * @return Response The HTTP response corresponding to the exception
     */
    @Override
    public Response toResponse(Throwable exception) {
        LOG.error("Global exception caught: ", exception);
        
        if (exception instanceof UnauthorizedException ex) {
            return handleUnauthorizedException(ex);
        }

        if (exception instanceof ValidationException ex) {
            return handleValidationException(ex);
        }
        
        if (exception instanceof ResourceNotFoundException ex) {
            return handleResourceNotFoundException(ex);
        }

        if (exception instanceof ForbiddenException ex) {
            return handleForbiddenException(ex);
        }

        if (exception instanceof ConflictException ex) {
            return handleConflictException(ex);
        }

        if (exception instanceof BadRequestException ex) {
            return handleBadRequestException(ex);
        }

        if (exception instanceof InsufficientFoundsException ex) {
            return handleInsufficientFoundsException(ex);
        }

        if (exception instanceof WebApplicationException ex) {
            return handleWebApplicationException(ex);
        }

        if (exception instanceof IllegalArgumentException ex) {
            return handleIllegalArgumentException(ex);
        }

        return handleGenericException(exception);
    }

    private Response handleIllegalArgumentException(IllegalArgumentException ex) {
        APIResponseDTO<Void> response = APIResponseDTO.error(
            "Invalid argument: " + ex.getMessage(), 
            400
        );
        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }

    private Response handleWebApplicationException(WebApplicationException ex) {
        int status = ex.getResponse().getStatus();
        String message = ex.getMessage();
        
        if (message == null || message.isEmpty()) {
            message = Response.Status.fromStatusCode(status).getReasonPhrase();
        }
        
        APIResponseDTO<Void> response = APIResponseDTO.error(message, status);
        return Response.status(status).entity(response).build();
    }

    private Response handleInsufficientFoundsException(InsufficientFoundsException ex) {
        LOG.warn("Insufficient funds error: " + ex.getMessage());
        String message = "Insufficient funds to complete the transaction";
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Exception details: ", ex);
            message += ": " + ex.getMessage();
        }

        APIResponseDTO<Void> response = APIResponseDTO.error(message, Response.Status.BAD_REQUEST.getStatusCode());
        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }

    private Response handleBadRequestException(BadRequestException ex) {
        LOG.warn("Bad request error: " + ex.getMessage());
        String message = "Bad request";
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Exception details: ", ex);
            message += ": " + ex.getMessage();
        }

        APIResponseDTO<Void> response = APIResponseDTO.error(message, Response.Status.BAD_REQUEST.getStatusCode());
        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }

    private Response handleConflictException(ConflictException ex) {
        LOG.warn("Conflict error: " + ex.getMessage());
        String message = "Conflict occurred";
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Exception details: ", ex);
            message += ": " + ex.getMessage();
        }

        APIResponseDTO<Void> response = APIResponseDTO.error(message, Response.Status.CONFLICT.getStatusCode());
        return Response.status(Response.Status.CONFLICT).entity(response).build();
    }

    private Response handleValidationException(ValidationException ex) {
        LOG.warn("Validation error: " + ex.getMessage());
        String message = "Validation failed";
        
        if (!ex.getValidationErrors().isEmpty()) {
            message += ": " + String.join(", ", ex.getValidationErrors());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Exception details: ", ex);
            message += ": " + ex.getMessage();
        }

        APIResponseDTO<Void> response = APIResponseDTO.error(message, Response.Status.BAD_REQUEST.getStatusCode());
        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }

    private Response handleForbiddenException(ForbiddenException ex) {
        LOG.warn("Forbidden access attempt: " + ex.getMessage());
        String message = "Access forbidden";
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Exception details: ", ex);
            message += ": " + ex.getMessage();
        }

        APIResponseDTO<Void> response = APIResponseDTO.error(message, Response.Status.FORBIDDEN.getStatusCode());
        return Response.status(Response.Status.FORBIDDEN).entity(response).build();
    }

    private Response handleResourceNotFoundException(ResourceNotFoundException ex) {
        LOG.warn("Resource not found: " + ex.getMessage());
        String message = "Resource not found";
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Exception details: ", ex);
            message += ": " + ex.getMessage();
        }

        APIResponseDTO<Void> response = APIResponseDTO.error(message, Response.Status.NOT_FOUND.getStatusCode());
        return Response.status(Response.Status.NOT_FOUND).entity(response).build();
    }

    private Response handleUnauthorizedException(UnauthorizedException ex) {
        LOG.warn("Unauthorized access attempt: " + ex.getMessage());
        String message = "Unauthorized access";
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Exception details: ", ex);
            message += ": " + ex.getMessage();
        }

        APIResponseDTO<Void> response = APIResponseDTO.error(message, Response.Status.UNAUTHORIZED.getStatusCode());
        return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
    }
    
    private Response handleGenericException(Throwable ex) {
        LOG.error("Unhandled exception occurred", ex);
        String message = "An unexpected error occurred";

        if (LOG.isDebugEnabled()) {
            LOG.debug("Exception details: ", ex);
            message += ": " + ex.getMessage();
        }

        APIResponseDTO<Void> response = APIResponseDTO.error(message, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
    }
}

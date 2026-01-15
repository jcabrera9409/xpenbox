package org.xpenbox.exception.handler;

import org.jboss.logging.Logger;
import org.xpenbox.common.dto.APIResponseDTO;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {
    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    @Override
    public Response toResponse(Throwable exception) {
        LOG.error("Global exception caught: ", exception);
        
        return handleGenericException(exception);
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

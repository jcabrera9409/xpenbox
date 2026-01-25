package org.xpenbox.user.controller;

import org.jboss.logging.Logger;
import org.xpenbox.common.dto.APIResponseDTO;
import org.xpenbox.user.dto.UserResponseDTO;
import org.xpenbox.user.service.IUserService;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/user")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserController {
    private static final Logger LOG = Logger.getLogger(UserController.class);

    private final IUserService userService;

    public UserController(
        IUserService userService
    ) {
        this.userService = userService;
    }

    @GET
    @Path("/me")
    public Response getUserInfo(@Context SecurityContext securityContext) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Get user info request received for user: %s", userEmail);

        UserResponseDTO userResponse = userService.getUserByEmail(userEmail);
        LOG.infof("User info retrieved successfully for user: %s", userEmail);

        return Response.ok(
            APIResponseDTO.success("User retrieved successfully", userResponse, Response.Status.OK.getStatusCode())
        ).build();
    }
    
}

package org.xpenbox.notifications.controller;

import org.jboss.logging.Logger;
import org.xpenbox.notifications.dto.DeviceTokenCreateDTO;
import org.xpenbox.notifications.service.IDeviceTokenService;

import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/device")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DeviceTokenController {
    private static final Logger LOG = Logger.getLogger(DeviceTokenController.class);

    private final IDeviceTokenService deviceTokenService;

    public DeviceTokenController(
        IDeviceTokenService deviceTokenService
    ) {
        this.deviceTokenService = deviceTokenService;
    }

    @POST
    @Transactional
    public Response registerDevice(@Context SecurityContext securityContext, @Valid DeviceTokenCreateDTO dto) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Register device token request received for user: %s", userEmail);
        deviceTokenService.create(dto, userEmail);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/test")
    public Response testNotification(@Context SecurityContext securityContext) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Test notification request received for user: %s", userEmail);
        deviceTokenService.sendTestNotification(userEmail);
        return Response.ok().build();
    }
}

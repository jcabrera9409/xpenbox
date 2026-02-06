package org.xpenbox.dashboard.controller;

import org.jboss.logging.Logger;
import org.xpenbox.common.dto.APIResponseDTO;
import org.xpenbox.dashboard.dto.DashboardResponseDTO;
import org.xpenbox.dashboard.dto.PeriodFilter;
import org.xpenbox.dashboard.service.IDashboardService;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/dashboard")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DashboardController {
    private final Logger LOG = Logger.getLogger(DashboardController.class);

    private final IDashboardService dashboardService;

    public DashboardController(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;  
    }

    @GET
    public Response generateDashboardData(@Context SecurityContext securityContext, @QueryParam("period") PeriodFilter periodFilter) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Dashboard data request received for user: %s", userEmail);

        DashboardResponseDTO dashboardData = dashboardService.generateDashboardData(periodFilter, userEmail);
        LOG.infof("Dashboard data generated successfully for user: %s", userEmail);

        return Response.ok(
            APIResponseDTO.success("Dashboard data retrieved successfully", dashboardData, Response.Status.OK.getStatusCode())
        ).build();
    }
    
}

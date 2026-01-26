package org.xpenbox.income.controller;

import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.common.dto.APIResponseDTO;
import org.xpenbox.income.dto.IncomeCreateDTO;
import org.xpenbox.income.dto.IncomeResponseDTO;
import org.xpenbox.income.dto.IncomeUpdateDTO;
import org.xpenbox.income.service.IIncomeService;

import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * IncomeController handles HTTP requests related to income operations.
 * It provides endpoints for creating, updating, retrieving all incomes,
 * and retrieving a specific income by its resource code.
 */
@Path("/income")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class IncomeController {
    private static final Logger LOG = Logger.getLogger(IncomeController.class);

    private final IIncomeService incomeService;

    public IncomeController(
        IIncomeService incomeService
    ) {
        this.incomeService = incomeService;
    }

    /**
     * Create a new income
     * @param securityContext Security context of the authenticated user
     * @param incomeCreateDTO Data transfer object containing income creation details
     * @return Response containing the created income information
     */
    @POST
    @Transactional
    public Response createIncome(@Context SecurityContext securityContext, @Valid IncomeCreateDTO incomeCreateDTO) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Create income request received for user: %s", userEmail);

        IncomeResponseDTO incomeResponse = incomeService.create(incomeCreateDTO, userEmail);
        LOG.infof("Income created successfully for user: %s", userEmail);

        return Response.status(Response.Status.CREATED).entity(
            APIResponseDTO.success("Income created successfully", incomeResponse, Response.Status.CREATED.getStatusCode())
        ).build();
    } 

    /**
     * Update an existing income
     * @param securityContext Security context of the authenticated user
     * @param resourceCode Unique resource code of the income to be updated
     * @param incomeUpdateDTO Data transfer object containing income update details
     * @return Response containing the updated income information
     */
    @PUT
    @Path("/{resourceCode}")
    @Transactional
    public Response updateIncome(@Context SecurityContext securityContext, @PathParam("resourceCode") String resourceCode, @Valid IncomeUpdateDTO incomeUpdateDTO) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Update income request received for user: %s, resourceCode: %s", userEmail, resourceCode);

        IncomeResponseDTO incomeResponse = incomeService.update(resourceCode, incomeUpdateDTO, userEmail);
        LOG.infof("Income updated successfully for user: %s, resourceCode: %s", userEmail, resourceCode);

        return Response.status(Response.Status.OK).entity(
            APIResponseDTO.success("Income updated successfully", incomeResponse, Response.Status.OK.getStatusCode())
        ).build();
    }

    /**
     * Get all incomes for the authenticated user
     * @param securityContext Security context of the authenticated user
     * @return Response containing the list of all incomes
     */
    @GET
    public Response getAllIncomes(@Context SecurityContext securityContext) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Get all incomes request received for user: %s", userEmail);

        var incomeResponses = incomeService.getAll(userEmail);
        LOG.infof("Retrieved %d incomes for user: %s", incomeResponses.size(), userEmail);

        return Response.status(Response.Status.OK).entity(
            APIResponseDTO.success("Incomes retrieved successfully", incomeResponses, Response.Status.OK.getStatusCode())
        ).build();
    }
    
    /**
     * Get income by resource code
     * @param securityContext Security context of the authenticated user
     * @param resourceCode Unique resource code of the income to be retrieved
     * @return Response containing the income information
     */
    @GET
    @Path("/{resourceCode}")
    public Response getIncomeByResourceCode(@Context SecurityContext securityContext, @PathParam("resourceCode") String resourceCode) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Get income by resource code request received for user: %s, resourceCode: %s", userEmail, resourceCode);

        IncomeResponseDTO incomeResponse = incomeService.getByResourceCode(resourceCode, userEmail);
        LOG.infof("Income retrieved successfully for user: %s, resourceCode: %s", userEmail, resourceCode);

        return Response.status(Response.Status.OK).entity(
            APIResponseDTO.success("Income retrieved successfully", incomeResponse, Response.Status.OK.getStatusCode())
        ).build();
    }

    /**
     * Filter incomes by date range
     * @param securityContext Security context of the authenticated user
     * @param startDateTimestamp Start date timestamp in milliseconds
     * @param endDateTimestamp End date timestamp in milliseconds
     * @return Response containing the list of filtered incomes
     */
    @GET
    @Path("/filter")
    public Response filterIncomesByDateRange(@Context SecurityContext securityContext, @QueryParam("from") Long startDateTimestamp, @QueryParam("to") Long endDateTimestamp) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Filter incomes by date range request received for user: %s, startDate: %s, endDate: %s", userEmail, startDateTimestamp, endDateTimestamp);

        List<IncomeResponseDTO> incomeResponses = incomeService.filterIncomesByDateRange(userEmail, startDateTimestamp, endDateTimestamp);
        LOG.infof("Filtered %d incomes for user: %s between %s and %s", incomeResponses.size(), userEmail, startDateTimestamp, endDateTimestamp);
        
        return Response.status(Response.Status.OK).entity(
            APIResponseDTO.success("Incomes filtered successfully", incomeResponses, Response.Status.OK.getStatusCode())
        ).build();
    }
}

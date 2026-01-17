package org.xpenbox.creditcard.controller;

import org.jboss.logging.Logger;
import org.xpenbox.common.dto.APIResponseDTO;
import org.xpenbox.creditcard.dto.CreditCardCreateDTO;
import org.xpenbox.creditcard.dto.CreditCardResponseDTO;
import org.xpenbox.creditcard.dto.CreditCardUpdateDTO;
import org.xpenbox.creditcard.service.ICreditCardService;

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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * CreditCardController handles HTTP requests related to credit card operations.
 * It provides endpoints for creating, updating, retrieving all credit cards,
 * and retrieving a specific credit card by its resource code.
 */
@Path("/creditcard")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CreditCardController {
    private static final Logger LOG = Logger.getLogger(CreditCardController.class);

    private final ICreditCardService creditCardService;

    public CreditCardController(
        ICreditCardService creditCardService
    ) {
        this.creditCardService = creditCardService;
    }

    /**
     * Create a new credit card
     * @param securityContext Security context of the authenticated user
     * @param creditCardCreateDTO Data transfer object containing credit card creation details
     * @return Response containing the created credit card information
     */
    @POST
    @Transactional
    public Response createCreditCard(@Context SecurityContext securityContext, @Valid CreditCardCreateDTO creditCardCreateDTO) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Create credit card request received for user: %s", userEmail);

        CreditCardResponseDTO creditCardResponse = creditCardService.create(creditCardCreateDTO, userEmail);
        LOG.infof("Credit card created successfully for user: %s", userEmail);

        return Response.status(Response.Status.CREATED).entity(
            APIResponseDTO.success("Credit card created successfully", creditCardResponse, Response.Status.CREATED.getStatusCode())
        ).build();
    }

    /**
     * Update an existing credit card
     * @param securityContext Security context of the authenticated user
     * @param resourceCode Unique resource code of the credit card to be updated
     * @param creditCardUpdateDTO Data transfer object containing credit card update details
     * @return Response containing the updated credit card information
     */
    @PUT
    @Path("/{resourceCode}")
    @Transactional
    public Response updateCreditCard(@Context SecurityContext securityContext, @PathParam("resourceCode") String resourceCode, @Valid CreditCardUpdateDTO creditCardUpdateDTO) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Update credit card request received for user: %s", userEmail);

        CreditCardResponseDTO creditCardResponse = creditCardService.update(resourceCode, creditCardUpdateDTO, userEmail);
        LOG.infof("Credit card updated successfully for user: %s", userEmail);

        return Response.ok(
            APIResponseDTO.success("Credit card updated successfully", creditCardResponse, Response.Status.OK.getStatusCode())
        ).build();
    }

    /**
     * Get all credit cards for the authenticated user
     * @param securityContext Security context of the authenticated user
     * @return Response containing the list of all credit cards
     */
    @GET
    public Response getAllCreditCards(@Context SecurityContext securityContext) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Get all credit cards request received for user: %s", userEmail);

        var creditCardResponses = creditCardService.getAll(userEmail);
        LOG.infof("Retrieved %d credit cards for user: %s", creditCardResponses.size(), userEmail);

        return Response.ok(
            APIResponseDTO.success("Credit cards retrieved successfully", creditCardResponses, Response.Status.OK.getStatusCode())
        ).build();
    }

    /**
     * Get a specific credit card by its resource code
     * @param securityContext Security context of the authenticated user
     * @param resourceCode Unique resource code of the credit card to be retrieved
     * @return Response containing the credit card information
     */
    @GET
    @Path("/{resourceCode}")
    public Response getCreditCardByResourceCode(@Context SecurityContext securityContext, @PathParam("resourceCode") String resourceCode) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Get credit card by resource code request received for user: %s", userEmail);

        CreditCardResponseDTO creditCardResponse = creditCardService.getByResourceCode(resourceCode, userEmail);
        LOG.infof("Credit card retrieved successfully for user: %s", userEmail);

        return Response.ok(
            APIResponseDTO.success("Credit card retrieved successfully", creditCardResponse, Response.Status.OK.getStatusCode())
        ).build();
    }
}

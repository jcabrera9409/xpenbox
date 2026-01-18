package org.xpenbox.transaction.controller;

import org.jboss.logging.Logger;
import org.xpenbox.common.dto.APIResponseDTO;
import org.xpenbox.transaction.dto.TransactionCreateDTO;
import org.xpenbox.transaction.dto.TransactionResponseDTO;
import org.xpenbox.transaction.service.ITransactionService;

import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/transaction")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionController {
    private static final Logger LOG = Logger.getLogger(TransactionController.class);

    private final ITransactionService transactionService;

    public TransactionController(
        ITransactionService transactionService
    ) {
        this.transactionService = transactionService;
    }

    @POST
    @Transactional
    public Response createTransaction(@Context SecurityContext securityContext, @Valid TransactionCreateDTO transactionCreateDTO) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Create transaction request received for user: %s", userEmail);

        TransactionResponseDTO transactionResponse = transactionService.create(transactionCreateDTO, userEmail);
        LOG.infof("Transaction created successfully for user: %s", userEmail);

        return Response.status(Response.Status.CREATED).entity(
            APIResponseDTO.success("Transaction created successfully", transactionResponse, Response.Status.CREATED.getStatusCode())
        ).build();
    }

    @GET
    @Path("/{resourceCode}")
    public Response getTransactionByResourceCode(@Context SecurityContext securityContext, @PathParam("resourceCode") String resourceCode) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Get transaction by resource code request received for user: %s, resourceCode: %s", userEmail, resourceCode);

        TransactionResponseDTO transactionResponse = transactionService.getByResourceCode(resourceCode, userEmail);
        LOG.infof("Transaction retrieved successfully for user: %s, resourceCode: %s", userEmail, resourceCode);

        return Response.status(Response.Status.OK).entity(
            APIResponseDTO.success("Transaction retrieved successfully", transactionResponse, Response.Status.OK.getStatusCode())
        ).build();
    }

    @DELETE
    @Path("/{resourceCode}")
    @Transactional
    public Response rollbackTransactionByResourceCode(@Context SecurityContext securityContext, @PathParam("resourceCode") String resourceCode) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Rollback transaction by resource code request received for user: %s, resourceCode: %s", userEmail, resourceCode);

        transactionService.rollback(resourceCode, userEmail);
        LOG.infof("Transaction rolled back successfully for user: %s, resourceCode: %s", userEmail, resourceCode);

        return Response.status(Response.Status.OK).entity(
            APIResponseDTO.success("Transaction rolled back successfully", null, Response.Status.OK.getStatusCode())
        ).build();
    }

}

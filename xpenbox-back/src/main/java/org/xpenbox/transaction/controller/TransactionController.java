package org.xpenbox.transaction.controller;

import org.jboss.logging.Logger;
import org.xpenbox.common.dto.APIPageableDTO;
import org.xpenbox.common.dto.APIResponseDTO;
import org.xpenbox.transaction.dto.TransactionCreateDTO;
import org.xpenbox.transaction.dto.TransactionFilterDTO;
import org.xpenbox.transaction.dto.TransactionResponseDTO;
import org.xpenbox.transaction.dto.TransactionUpdateDTO;
import org.xpenbox.transaction.service.ITransactionService;

import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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

    /**
     * Create a new transaction
     * @param securityContext the security context containing user information
     * @param transactionCreateDTO the transaction data transfer object
     * @return a response indicating the result of the create operation
     */
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

    /**
     * Update an existing transaction
     * @param securityContext Security context of the authenticated user
     * @param resourceCode Unique resource code of the transaction to be updated
     * @param transactionUpdateDTO Data transfer object containing transaction update details
     * @return Response containing the updated transaction information
     */
    @PUT
    @Path("/{resourceCode}")
    @Transactional
    public Response updateTransaction(@Context SecurityContext securityContext, @PathParam("resourceCode") String resourceCode, @Valid TransactionUpdateDTO transactionUpdateDTO) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Update transaction request received for user: %s", userEmail);

        TransactionResponseDTO transactionResponse = transactionService.update(resourceCode, transactionUpdateDTO, userEmail);
        LOG.infof("Transaction updated successfully for user: %s", userEmail);
        
        return Response.ok(
            APIResponseDTO.success("Transaction updated successfully", transactionResponse, Response.Status.OK.getStatusCode())
        ).build();
    }

    /**
     * Filter transactions based on criteria
     * @param securityContext the security context containing user information
     * @param filterDTO the transaction filter data transfer object
     * @return a response containing the filtered transactions
     */
    @POST
    @Path("/filter")
    public Response filterTransactions(@Context SecurityContext securityContext, @Valid TransactionFilterDTO filterDTO) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Filter transactions request received for user: %s", userEmail);

        APIPageableDTO<TransactionResponseDTO> filteredTransactions = transactionService.filterTransactions(filterDTO, userEmail);
        LOG.infof("Filtered transactions retrieved successfully for user: %s", userEmail);

        return Response.status(Response.Status.OK).entity(
            APIResponseDTO.success("Filtered transactions retrieved successfully", filteredTransactions, Response.Status.OK.getStatusCode())
        ).build();
    }

    /**
     * Get transaction by resource code
     * @param securityContext the security context containing user information
     * @param resourceCode the resource code of the transaction
     * @return a response containing the transaction details
     */
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

    /**
     * Rollback transaction by resource code
     * @param securityContext the security context containing user information
     * @param resourceCode the resource code of the transaction to rollback
     * @return a response indicating the result of the rollback operation
     */
    @DELETE
    @Path("/{resourceCode}")
    @Transactional
    public Response rollbackTransactionByResourceCode(@Context SecurityContext securityContext, @PathParam("resourceCode") String resourceCode) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Rollback transaction by resource code request received for user: %s, resourceCode: %s", userEmail, resourceCode);

        transactionService.rollback(resourceCode, userEmail);
        LOG.infof("Transaction rolled back successfully for user: %s, resourceCode: %s", userEmail, resourceCode);

        return Response.status(Response.Status.NO_CONTENT).entity(
            APIResponseDTO.success("Transaction rolled back successfully", null, Response.Status.NO_CONTENT.getStatusCode())
        ).build();
    }

}

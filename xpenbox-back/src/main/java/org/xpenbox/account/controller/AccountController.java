package org.xpenbox.account.controller;

import org.jboss.logging.Logger;
import org.xpenbox.account.dto.AccountCreateDTO;
import org.xpenbox.account.dto.AccountResponseDTO;
import org.xpenbox.account.dto.AccountUpdateDTO;
import org.xpenbox.account.service.IAccountService;
import org.xpenbox.common.dto.APIResponseDTO;

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

@Path("/account")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountController {
    private static final Logger LOG = Logger.getLogger(AccountController.class);

    private final IAccountService accountService;

    public AccountController(
        IAccountService accountService
    ) {
        this.accountService = accountService;
    }
    
    @POST
    @Transactional
    public Response createAccount(@Context SecurityContext securityContext, @Valid AccountCreateDTO accountCreateDTO) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Create account request received for user: %s", userEmail);

        AccountResponseDTO accountResponse = accountService.create(accountCreateDTO, userEmail);
        LOG.infof("Account created successfully for user: %s", userEmail);

        return Response.status(Response.Status.CREATED).entity(
            APIResponseDTO.success("Account created successfully", accountResponse, Response.Status.CREATED.getStatusCode())
        ).build();
    }

    @PUT
    @Path("/{resourceCode}")
    @Transactional
    public Response updateAccount(@Context SecurityContext securityContext, @PathParam("resourceCode") String resourceCode, @Valid AccountUpdateDTO accountUpdateDTO) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Update account request received for user: %s, resourceCode: %s", userEmail, resourceCode);

        AccountResponseDTO accountResponse = accountService.update(resourceCode, accountUpdateDTO, userEmail);
        LOG.infof("Account updated successfully for user: %s, resourceCode: %s", userEmail, resourceCode);

        return Response.status(Response.Status.OK).entity(
            APIResponseDTO.success("Account updated successfully", accountResponse, Response.Status.OK.getStatusCode())
        ).build();
    }

    @GET
    public Response getAllAccounts(@Context SecurityContext securityContext) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Get all accounts request received for user: %s", userEmail);

        var accounts = accountService.getAll(userEmail);
        LOG.infof("Retrieved %d accounts for user: %s", accounts.size(), userEmail);

        return Response.status(Response.Status.OK).entity(
            APIResponseDTO.success("Accounts retrieved successfully", accounts, Response.Status.OK.getStatusCode())
        ).build();
    }

    @GET
    @Path("/{resourceCode}")
    public Response getAccountByResourceCode(@Context SecurityContext securityContext, @PathParam("resourceCode") String resourceCode) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Get account by resourceCode request received for user: %s, resourceCode: %s", userEmail, resourceCode);

        AccountResponseDTO accountResponse = accountService.getByResourceCode(resourceCode, userEmail);
        LOG.infof("Account retrieved successfully for user: %s, resourceCode: %s", userEmail, resourceCode);

        return Response.status(Response.Status.OK).entity(
            APIResponseDTO.success("Account retrieved successfully", accountResponse, Response.Status.OK.getStatusCode())
        ).build();
    }
}

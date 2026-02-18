package org.xpenbox.payment.controller;

import org.jboss.logging.Logger;
import org.xpenbox.common.dto.APIResponseDTO;
import org.xpenbox.payment.dto.PreApprovalSubscriptionRequestDTO;
import org.xpenbox.payment.dto.PreApprovalSubscriptionResponseDTO;
import org.xpenbox.payment.dto.SubscriptionResponseDTO;
import org.xpenbox.payment.service.ISubscriptionService;

import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * SubscriptionController is responsible for handling all subscription-related HTTP requests, including creating pre-approval plans. It uses the ISubscriptionService to perform business logic and interacts with the authenticated user's security context to ensure proper authorization and access control.
 */
@Path("/subscription")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SubscriptionController {
    private static final Logger LOG = Logger.getLogger(SubscriptionController.class);

    private final ISubscriptionService subscriptionService;

    public SubscriptionController(
        ISubscriptionService subscriptionService
    ) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Retrieves the active subscription for the authenticated user. This endpoint checks the security context to identify the user making the request and then calls the subscription service to fetch the active subscription details. The response is returned in a standardized API response format, indicating success or failure along with the subscription data if available.
     * @param securityContext the security context containing user information
     * @return a response indicating the result of the get active subscription operation, including the subscription details if successful
     */
    @GET
    @Path("/me")
    @Transactional
    public Response getMySubscription(@Context SecurityContext securityContext) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Get subscription request received for user: %s", userEmail);

        SubscriptionResponseDTO response = subscriptionService.getActiveSubscription(userEmail);

        return Response.ok(
            APIResponseDTO.success("Subscription retrieved successfully", response, Response.Status.OK.getStatusCode())
        ).build();
    }

    /**
     * Create a new pre-approval subscription for the authenticated user
     * @param securityContext the security context containing user information
     * @param request the pre-approval subscription request data transfer object
     * @return a response indicating the result of the create pre-approval subscription operation
     */
    @POST
    @Path("/pre-approval")
    @Transactional
    public Response createPreApprovalSubscription(@Context SecurityContext securityContext, @Valid PreApprovalSubscriptionRequestDTO request) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Create pre-approval subscription request received for user: %s", userEmail);

        PreApprovalSubscriptionResponseDTO response = subscriptionService.createPreApprovalSubscription(request, userEmail);
        LOG.infof("Pre-approval subscription created successfully for user: %s", userEmail);

        return Response.ok(
            APIResponseDTO.success("Pre-approval subscription created successfully", response, Response.Status.OK.getStatusCode())
        ).build();
    }

    @POST
    @Path("/cancel")
    @Transactional
    public Response cancelSubscription(@Context SecurityContext securityContext) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Cancel subscription request received for user: %s", userEmail);

        subscriptionService.cancelActiveSubscription(userEmail);
        LOG.infof("Subscription cancelled successfully for user: %s", userEmail);

        return Response.ok(
            APIResponseDTO.success("Subscription cancelled successfully", null, Response.Status.OK.getStatusCode())
        ).build();
    }

}

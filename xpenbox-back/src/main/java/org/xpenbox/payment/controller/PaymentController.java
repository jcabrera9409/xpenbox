package org.xpenbox.payment.controller;

import org.jboss.logging.Logger;
import org.xpenbox.common.dto.APIResponseDTO;
import org.xpenbox.payment.dto.PreApprovalSubscriptionRequestDTO;
import org.xpenbox.payment.dto.PreApprovalSubscriptionResponseDTO;
import org.xpenbox.payment.service.IPaymentService;

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

/**
 * PaymentController is responsible for handling all payment-related HTTP requests, including creating pre-approval plans. It uses the IPaymentService to perform business logic and interacts with the authenticated user's security context to ensure proper authorization and access control.
 */
@Path("/payment")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PaymentController {
    private static final Logger LOG = Logger.getLogger(PaymentController.class);

    private final IPaymentService paymentService;

    public PaymentController(
        IPaymentService paymentService
    ) {
        this.paymentService = paymentService;
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

        PreApprovalSubscriptionResponseDTO response = paymentService.createPreApprovalSubscription(request, userEmail);
        LOG.infof("Pre-approval subscription created successfully for user: %s", userEmail);

        return Response.ok(
            APIResponseDTO.success("Pre-approval subscription created successfully", response, Response.Status.OK.getStatusCode())
        ).build();
    }

}

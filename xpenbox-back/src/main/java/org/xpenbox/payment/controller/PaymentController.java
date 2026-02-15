package org.xpenbox.payment.controller;

import org.jboss.logging.Logger;
import org.xpenbox.common.dto.APIResponseDTO;
import org.xpenbox.payment.dto.PreApprovalPlanRequestDTO;
import org.xpenbox.payment.dto.PreApprovalPlanResponseDTO;
import org.xpenbox.payment.service.IPaymentService;

import io.quarkus.security.Authenticated;
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
     * Create a new pre-approval plan for the authenticated user
     * @param securityContext the security context containing user information
     * @param request the pre-approval plan request data transfer object
     * @return a response indicating the result of the create pre-approval plan operation
     */
    @POST
    @Path("/pre-approval-plan")
    public Response createPreApprovalPlan(@Context SecurityContext securityContext, @Valid PreApprovalPlanRequestDTO request) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Create pre-approval plan request received for user: %s", userEmail);

        PreApprovalPlanResponseDTO response = paymentService.createPreApprovalPlan(request, userEmail);
        LOG.infof("Pre-approval plan created successfully for user: %s", userEmail);

        return Response.ok(
            APIResponseDTO.success("Pre-approval plan created successfully", response, Response.Status.OK.getStatusCode())
        ).build();
    }

}

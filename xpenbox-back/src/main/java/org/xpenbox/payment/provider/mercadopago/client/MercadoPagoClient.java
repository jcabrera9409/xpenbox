package org.xpenbox.payment.provider.mercadopago.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalRequestDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalResponseDTO;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * MercadoPagoClient is a REST client interface for interacting with the MercadoPago API. It defines methods for creating subscriptions and handling responses from the API.
 */
@RegisterRestClient(configKey = "mp-api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface MercadoPagoClient {
    
    /**
     * Creates a subscription (pre-approval plan) in MercadoPago based on the provided request data. This method sends a POST request to the MercadoPago API and expects a response containing details about the created subscription, including the approval URL for the user to complete the subscription process.
     * @param request the MPApprovalRequestDTO containing the necessary information to create a subscription in MercadoPago, such as reason, external reference, payer email, back URL, amount, and currency
     * @return an MPApprovalResponseDTO containing details about the created subscription, including the approval URL for the user to complete the subscription process
     */
    @POST
    @Path("/preapproval")
    MPApprovalResponseDTO createSubscription(MPApprovalRequestDTO request);
}

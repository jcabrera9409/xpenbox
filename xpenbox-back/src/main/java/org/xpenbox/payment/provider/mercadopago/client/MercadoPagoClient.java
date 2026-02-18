package org.xpenbox.payment.provider.mercadopago.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalSubscriptionRequestDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalSubscriptionResponseDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPPaymentResponseDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPUpdateSubscriptionRequestDTO;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
     * Retrieves the details of a subscription (pre-approval) from MercadoPago based on the provided subscription ID. This method sends a GET request to the MercadoPago API and expects a response containing details about the subscription, such as its status, reason, external reference, payer email, back URL, amount, and currency.
     * @param id the ID of the subscription (pre-approval) to be retrieved from MercadoPago
     * @return an MPApprovalSubscriptionResponseDTO containing details about the subscription (pre-approval) retrieved from MercadoPago, such as its status, reason, external reference, payer email, back URL, amount, and currency
     */
    @GET
    @Path("/v1/preapproval/{id}")
    MPApprovalSubscriptionResponseDTO getSubscription(@PathParam("id") String id);

    /**
     * Creates a subscription (pre-approval plan) in MercadoPago based on the provided request data. This method sends a POST request to the MercadoPago API and expects a response containing details about the created subscription, including the approval URL for the user to complete the subscription process.
     * @param request the MPApprovalRequestDTO containing the necessary information to create a subscription in MercadoPago, such as reason, external reference, payer email, back URL, amount, and currency
     * @return an MPApprovalResponseDTO containing details about the created subscription, including the approval URL for the user to complete the subscription process
     */
    @POST
    @Path("/preapproval")
    MPApprovalSubscriptionResponseDTO createSubscription(MPApprovalSubscriptionRequestDTO request);

    /**
     * Updates an existing subscription in MercadoPago based on the provided subscription ID and request data. This method sends a PUT request to the MercadoPago API and expects a response containing details about the updated subscription.
     * @param id the ID of the subscription to be updated in MercadoPago
     * @param request the MPUpdateSubscriptionRequestDTO containing the necessary information to update the subscription in MercadoPago, such as reason, external reference, payer email, back URL, amount, and currency
     * @return an MPApprovalSubscriptionResponseDTO containing details about the updated subscription, including the approval URL for the user to complete the subscription process if applicable
     */
    @PUT
    @Path("/preapproval/{id}")
    MPApprovalSubscriptionResponseDTO updateSubscription(@PathParam("id") String id, MPUpdateSubscriptionRequestDTO request);

    /**
     * Retrieves the details of a subscription (payment) from MercadoPago based on the provided subscription ID. This method sends a GET request to the MercadoPago API and expects a response containing details about the subscription, such as its status, reason, external reference, payer email, back URL, amount, and currency.
     * @param id the ID of the subscription (payment) to be retrieved from MercadoPago
     * @return an MPPaymentResponseDTO containing details about the subscription (payment) retrieved from MercadoPago, such as its status, reason, external reference, payer email, back URL, amount, and currency
     */
    @GET
    @Path("/v1/payments/{id}")
    MPPaymentResponseDTO getPayment(@PathParam("id") String id);
}

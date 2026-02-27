package org.xpenbox.payment.controller;

import org.jboss.logging.Logger;
import org.xpenbox.payment.enums.PaymentProviderType;
import org.xpenbox.payment.provider.mercadopago.dto.MPWebhookRequestDTO;
import org.xpenbox.payment.service.IWebhookService;

import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Controller class responsible for handling incoming webhooks from payment providers. It defines endpoints for each provider and delegates the processing of the webhook to the appropriate service.
 */
@Path("/webhook")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebhookController {
    private static final Logger LOG = Logger.getLogger(WebhookController.class);

    private final IWebhookService webhookService;

    public WebhookController(IWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /**
     * Endpoint to handle incoming webhooks from MercadoPago. It validates the webhook signature and processes the payload accordingly.
     * @param signature The HMAC signature sent by MercadoPago in the "x-signature" header, used for validating the authenticity of the webhook.
     * @param requestId The unique request ID sent by MercadoPago in the "x-request-id" header, used for tracking and logging purposes.
     * @param dataId The ID of the data object related to the webhook event, sent as a query parameter "data.id".
     * @param type The type of the webhook event, sent as a query parameter "type".
     * @param payload The JSON payload of the webhook, mapped to the MPWebhookRequestDTO class, containing the details of the webhook event.
     * @return A Response object indicating the result of processing the webhook. Returns HTTP 200 OK if the webhook is processed successfully, or an appropriate error response if validation fails or an exception occurs.
     */
    @POST
    @Path("/mercadopago")
    @PermitAll
    @Transactional
    public Response handleMercadoPagoWebhook(
        @HeaderParam("x-signature") String signature,
        @HeaderParam("x-request-id") String requestId,
        @QueryParam("data.id") String dataId,
        MPWebhookRequestDTO payload
    ) {
        LOG.info("Received MercadoPago webhook with payload: " + payload);
        LOG.debugf("Webhook headers - x-signature: %s, x-request-id: %s, data.id: %s", signature, requestId, dataId);
        webhookService.validateWebhook(PaymentProviderType.MERCADOPAGO, signature, requestId, dataId);
        
        LOG.debugf("Webhook payload: %s", payload);
        webhookService.registerNewPaymentProviderWebhook(PaymentProviderType.MERCADOPAGO, payload);
        
        return Response.ok().build();
    }    
}

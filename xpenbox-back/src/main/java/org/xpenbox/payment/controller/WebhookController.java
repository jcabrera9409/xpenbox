package org.xpenbox.payment.controller;

import org.jboss.logging.Logger;

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

@Path("/webhook")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebhookController {
    private static final Logger LOG = Logger.getLogger(WebhookController.class);

    @POST
    @Path("/mercadopago")
    @PermitAll
    @Transactional
    public Response handleMercadoPagoWebhook(
        @HeaderParam("x-signature") String signature,
        @HeaderParam("x-request-id") String requestId,
        @QueryParam("data.id") String dataId,
        @QueryParam("type") String type,
        String payload
    ) {
        LOG.info("Received MercadoPago webhook with payload: " + payload);

        LOG.debugf("Webhook headers - x-signature: %s, x-request-id: %s, data.id: %s, type: %s", signature, requestId, dataId, type);
        LOG.debugf("Webhook payload: %s", payload);

        return Response.ok().build();
    }    
}

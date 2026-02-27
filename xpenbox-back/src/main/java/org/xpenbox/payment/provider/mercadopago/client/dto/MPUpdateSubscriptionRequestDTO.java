package org.xpenbox.payment.provider.mercadopago.client.dto;

/**
 * Data Transfer Object for updating a subscription in MercadoPago.
 * @param status The new status of the subscription.
 */
public record MPUpdateSubscriptionRequestDTO (
    String status
) { }
package org.xpenbox.payment.provider.mercadopago.client.dto;

/**
 * Data Transfer Object for creating a subscription approval request in MercadoPago.
 * @param reason The reason for the subscription.
 * @param external_reference An external reference for the subscription.
 * @param payer_email The email of the payer.
 * @param auto_recurring The auto-recurring payment details.
 * @param back_url The URL to redirect the user after the approval process.
 */
public record MPApprovalSubscriptionRequestDTO (
    String reason,
    String external_reference,
    String payer_email,
    MPAutoRecurring auto_recurring,
    String back_url
) { }
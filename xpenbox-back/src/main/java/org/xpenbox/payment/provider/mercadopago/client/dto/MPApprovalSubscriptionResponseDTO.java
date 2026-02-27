package org.xpenbox.payment.provider.mercadopago.client.dto;

/**
 * Data Transfer Object for MercadoPago approval response. This DTO is used to encapsulate the data returned by the MercadoPago API when a pre-approval plan is created. It includes various fields that provide information about the payment, such as the unique identifiers for the payment, payer, collector, and application, as well as the status, reason, external reference, creation and modification dates, initialization URL, subscription ID (if applicable), and auto-recurring payment details (if applicable).
 * @param id Unique identifier of the payment.
 * @param payer_id Unique identifier of the payer.
 * @param payer_email Email of the payer.
 * @param back_url URL to redirect after payment.
 * @param collector_id Unique identifier of the collector.
 * @param application_id Unique identifier of the application.
 * @param status Status of the payment.
 * @param reason Reason for the payment status.
 * @param external_reference External reference for the payment.
 * @param date_created Date and time when the payment was created.
 * @param last_modified Date and time when the payment was last modified.
 * @param init_point URL to initialize the payment process.
 * @param subscription_id Unique identifier of the subscription (if applicable).
 * @param auto_recurring Auto-recurring payment details (if applicable).
 */
public record MPApprovalSubscriptionResponseDTO (
    String id, 
    Long payer_id,
    String payer_email,
    String back_url,
    Long collector_id,
    Long application_id,
    String status,
    String reason,
    String external_reference,
    String date_created,
    String last_modified,
    String init_point,
    String subscription_id,
    MPAutoRecurring auto_recurring
) { }

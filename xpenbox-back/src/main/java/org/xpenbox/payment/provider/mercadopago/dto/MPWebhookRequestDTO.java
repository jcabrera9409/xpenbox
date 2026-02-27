package org.xpenbox.payment.provider.mercadopago.dto;

/**
 * Data Transfer Object (DTO) representing the structure of a webhook request received from a payment provider. This DTO is designed to capture the essential information sent by the provider when a webhook event occurs, such as subscription updates or payment notifications. The MPWebhookRequestDTO includes fields for the event ID, action type, API version, creation date, live mode status, event type, user ID, and a nested data object containing additional details about the event.
 * @param id The unique identifier of the webhook event.
 * @param action The action associated with the webhook event (e.g., "subscription.created", "payment.completed").
 * @param api_version The version of the API used by the payment provider.
 * @param date_created The timestamp when the webhook event was created.
 * @param live_mode A boolean indicating whether the event occurred in live mode or test mode.
 * @param type The type of the webhook event (e.g., "subscription", "payment").
 * @param user_id The identifier of the user associated with the event.
 * @param data A nested object containing additional data related to the webhook event, such as the ID of the subscription or payment involved.
 */
public record MPWebhookRequestDTO (
    Long id,
    String action, 
    String api_version,
    String date_created,
    Boolean live_mode,
    String type,
    String user_id,
    MPWebhookDataDTO data
) { 
    public record MPWebhookDataDTO (
        String id
    ) { }
}


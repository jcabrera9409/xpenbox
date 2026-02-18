package org.xpenbox.payment.provider.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.xpenbox.payment.entity.SubscriptionPayment.PaymentStatus;

/**
 * Data Transfer Object (DTO) representing the response received from a payment provider when retrieving the details of a subscription (payment). This DTO captures essential information about the payment, such as its ID, amount, status, currency, approval date, and the associated subscription ID. The ProviderPaymentResponseDTO is designed to provide a standardized way to represent payment details across different payment providers, allowing for easier integration and handling of payment data in the application.
 * @param id The unique identifier of the payment in the payment provider's system.
 * @param amount The amount of the transaction associated with the payment.
 * @param status The current status of the payment (e.g., "approved", "pending", "rejected").
 * @param currency The currency code of the transaction amount (e.g., "USD", "ARS").
 * @param dateApproved The timestamp when the payment was approved.
 * @param subscriptionId The unique identifier of the subscription associated with the payment, which can be used to correlate the payment with specific subscription records in the system.
 */
public record ProviderPaymentResponseDTO (
    Long id,
    BigDecimal amount,
    PaymentStatus status,
    String rawStatus,
    String currency,
    LocalDateTime dateApproved,
    String subscriptionId
) { }

package org.xpenbox.payment.provider.dto;

import org.xpenbox.payment.enums.PaymentProviderType;

/**
 * Data Transfer Object for subscription response.
 * @param providerSubscriptionId The unique identifier for the subscription provided by the payment provider.
 * @param checkoutUrl The URL where the user can complete the subscription process.
 * @param status The current status of the subscription (e.g., "pending", "active", "cancelled").
 */
public record ProviderSubscriptionResponseDTO (
    String providerSubscriptionPlanId,
    String checkoutUrl,
    String status,
    PaymentProviderType paymentProviderType
) { }

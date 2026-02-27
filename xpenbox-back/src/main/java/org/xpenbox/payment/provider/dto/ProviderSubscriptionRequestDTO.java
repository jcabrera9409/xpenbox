package org.xpenbox.payment.provider.dto;

import java.math.BigDecimal;

import org.xpenbox.payment.entity.Plan.BillingCycle;
import org.xpenbox.payment.enums.PaymentProviderType;

/**
 * Data Transfer Object for subscription creation requests.
 * @param userId The ID of the user subscribing to the plan.
 * @param userEmail The email of the user subscribing to the plan.
 * @param planName The name of the subscription plan.
 * @param amount The amount to be charged for the subscription.
 * @param currency The currency in which the amount is specified.
 * @param paymentProviderType The type of payment provider to be used for the subscription.
 */
public record ProviderSubscriptionRequestDTO (
    Long userId,
    String userEmail,
    String planName,
    BigDecimal amount,
    String currency,
    BillingCycle billingCycle,
    PaymentProviderType paymentProviderType
) { }

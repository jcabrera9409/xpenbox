package org.xpenbox.payment.dto;

import java.math.BigDecimal;

import org.xpenbox.payment.entity.Subscription.SubscriptionStatus;
import org.xpenbox.user.dto.UserResponseDTO;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for Subscription response. This DTO is used to transfer subscription data from the server to the client.
 * @param resourceCode Unique code representing the subscription resource.
 * @param planPrice Price of the subscribed plan.
 * @param planCurrency Currency of the plan price.
 * @param startDateTimestamp Timestamp representing the start date of the subscription.
 * @param endDateTimestamp Timestamp representing the end date of the subscription (nullable).
 * @param nextBillingDateTimestamp Timestamp representing the next billing date of the subscription (nullable).
 * @param provider Name of the payment provider (nullable).
 * @param providerPlanId Identifier of the plan in the payment provider's system (nullable).
 * @param providerSubscriptionId Identifier of the subscription in the payment provider's system (nullable).
 * @param status Current status of the subscription.
 * @param plan Plan details associated with the subscription.
 * @param user User details associated with the subscription.
 */
@RegisterForReflection
public record SubscriptionResponseDTO (
    String resourceCode,
    BigDecimal planPrice,
    String planCurrency,
    Long startDateTimestamp,
    Long endDateTimestamp,
    Long nextBillingDateTimestamp,
    Boolean renew,
    String provider,
    String providerPlanId,
    String providerSubscriptionId,
    SubscriptionStatus status,
    PlanResponseDTO plan,
    UserResponseDTO user
) { }

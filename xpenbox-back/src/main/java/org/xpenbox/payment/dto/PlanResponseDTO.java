package org.xpenbox.payment.dto;

import java.math.BigDecimal;

import org.xpenbox.payment.entity.Plan.BillingCycle;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for Plan response. This DTO is used to transfer plan data from the server to the client.
 * @param resourceCode Unique code for the plan, used as an identifier in the payment provider.
 * @param name Name of the plan.
 * @param description Description of the plan.
 * @param price Price of the plan.
 * @param currency Currency of the plan price (e.g., USD, EUR).
 * @param billingCycle Billing cycle of the plan (e.g., MONTHLY, YEARLY).
 */
@RegisterForReflection
public record PlanResponseDTO (
    String resourceCode,
    String name,
    String description,
    BigDecimal price,
    String currency,
    BillingCycle billingCycle
) { }

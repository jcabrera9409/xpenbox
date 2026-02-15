package org.xpenbox.payment.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for the response of a pre-approval plan creation. This DTO contains the URL that the user should be redirected to in order to approve the subscription plan. The initPointUrl is typically provided by the payment provider after creating a pre-approval plan, and it is used to guide the user through the approval process for their subscription.
 * @param initPointUrl the URL to which the user should be redirected to approve the subscription plan
 */
@RegisterForReflection
public record PreApprovalPlanResponseDTO (
    String initPointUrl
) { }

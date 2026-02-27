package org.xpenbox.payment.dto;

import org.hibernate.validator.constraints.Length;
import org.xpenbox.payment.enums.PaymentProviderType;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for pre-approval plan request. This DTO is used to encapsulate the data required to create a pre-approval plan for a subscription. It includes validation annotations to ensure that the necessary fields are provided and meet certain criteria, such as not being null or blank and having a maximum length. The resource code of the plan is a crucial piece of information that identifies the specific plan for which the pre-approval is being requested.
 * @param resourceCodePlan the resource code of the plan for which the pre-approval is being requested, must not be null, blank, and must not exceed 100 characters in length
 * @param paymentProviderType the type of payment provider for the pre-approval plan, must not be null
 */
@RegisterForReflection
public record PreApprovalSubscriptionRequestDTO (

    @NotNull(message = "Resource code of the plan is required")
    @NotBlank(message = "Resource code of the plan must not be blank")
    @Length(max = 100, message = "Resource code must not exceed 100 characters")
    String resourceCodePlan,

    @NotNull(message = "Payment provider type is required")
    PaymentProviderType paymentProviderType
) { }


/**
 * Data Transfer Object (DTO) for the request to create a pre-approval subscription.
 * @param resourceCodePlan - The code of the subscription plan to which the user wants to subscribe.
 * @param paymentProviderType - The type of payment provider to be used for the subscription.
 */
export class PreApprovalSubscriptionRequestDTO {
    resourceCodePlan: string;
    paymentProviderType: string;
    constructor(resourceCodePlan: string, paymentProviderType: string) {
        this.resourceCodePlan = resourceCodePlan;
        this.paymentProviderType = paymentProviderType;
    }

    /**
     * Generates a request body for creating a pre-approval subscription using predefined constants for the plan and provider.
     * @returns A new instance of PreApprovalSubscriptionRequestDTO with the specified plan and provider.
     */
    static generateRequestBody(): PreApprovalSubscriptionRequestDTO {
        return new PreApprovalSubscriptionRequestDTO(SubscriptionConstants.plan, SubscriptionConstants.provider);
    }
}

/**
 * Constants used for subscription requests, including the payment provider and the subscription plan.
 * provider - The payment provider to be used for the subscription (e.g., 'MERCADOPAGO').
 * plan - The code of the subscription plan to which the user wants to subscribe (e.g., 'plan_pro_monthly').
 */
export const SubscriptionConstants = {
    provider: 'MERCADOPAGO',
    plan: 'plan_pro_monthly'
}

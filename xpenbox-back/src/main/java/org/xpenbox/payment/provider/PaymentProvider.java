package org.xpenbox.payment.provider;

import org.xpenbox.payment.dto.SubscriptionRequestDTO;
import org.xpenbox.payment.dto.SubscriptionResponseDTO;

/**
 * The PaymentProvider interface defines the contract for integrating with different payment providers. It includes methods for creating subscriptions and handling webhooks, allowing for a standardized way to interact with various payment services. Implementations of this interface will provide the specific logic for interacting with the APIs of different payment providers, such as Stripe, PayPal, or others.
 */
public interface PaymentProvider {

    /**
     * Creates a pre-approval plan for a user based on the provided subscription request. This method should interact with the payment provider's API to create the pre-approval plan and return relevant details in a SubscriptionResponseDTO.
     * @param subscriptionRequest the subscription request containing details about the user and plan
     * @return a SubscriptionResponseDTO containing details about the created pre-approval plan
     */
    SubscriptionResponseDTO createPreApprovalPlan(SubscriptionRequestDTO subscriptionRequest);

    /**
     * Handles incoming webhooks from the payment provider. This method should process the payload received from the payment provider, which may contain information about subscription events, payment updates, or other relevant notifications.
     * @param payload the raw payload received from the payment provider's webhook, which should be parsed and processed according to the provider's specifications
     */
    void handleWebhook(String payload);
}

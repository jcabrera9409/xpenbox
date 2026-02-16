package org.xpenbox.payment.provider;

import org.xpenbox.payment.provider.dto.ProviderSubscriptionRequestDTO;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionResponseDTO;

/**
 * The PaymentProvider interface defines the contract for integrating with different payment providers. It includes methods for creating subscriptions and handling webhooks, allowing for a standardized way to interact with various payment services. Implementations of this interface will provide the specific logic for interacting with the APIs of different payment providers, such as Stripe, PayPal, or others.
 */
public interface PaymentProvider {

    /**
     * Creates a pre-approval subscription for a user based on the provided subscription request. This method should interact with the payment provider's API to create the pre-approval subscription and return relevant details in a SubscriptionResponseDTO.
     * @param subscriptionRequest the ProviderSubscriptionRequestDTO containing the necessary information to create the pre-approval subscription, such as the plan details, user information, and payment provider type
     * @return a ProviderSubscriptionResponseDTO containing details about the created pre-approval subscription
     */
    ProviderSubscriptionResponseDTO createPreApprovalSubscription(ProviderSubscriptionRequestDTO subscriptionRequest);

    /**
     * Cancels an existing subscription based on the provided subscription ID. This method should interact with the payment provider's API to cancel the subscription and return relevant details in a SubscriptionResponseDTO.
     * @param subscriptionId the unique identifier of the subscription to be canceled, which should be provided by the payment provider when the subscription was created
     * @return a ProviderSubscriptionResponseDTO containing details about the canceled subscription, such as the cancellation status and any relevant information from the payment provider
     */
    ProviderSubscriptionResponseDTO cancelSubscription(String subscriptionId);

    /**
     * Handles incoming webhooks from the payment provider. This method should process the payload received from the payment provider, which may contain information about subscription events, payment updates, or other relevant notifications.
     * @param payload the raw payload received from the payment provider's webhook, which should be parsed and processed according to the provider's specifications
     */
    void handleWebhook(String payload);
}

package org.xpenbox.payment.provider;

import org.xpenbox.payment.provider.dto.ProviderPaymentResponseDTO;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionRequestDTO;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionResponseDTO;

/**
 * The PaymentProvider interface defines the contract for integrating with different payment providers. It includes methods for creating subscriptions and handling webhooks, allowing for a standardized way to interact with various payment services. Implementations of this interface will provide the specific logic for interacting with the APIs of different payment providers, such as Stripe, PayPal, or others.
 */
public interface PaymentProvider {

    /**
     * Retrieves the details of a subscription based on the provided subscription ID. This method should interact with the payment provider's API to fetch the subscription details and return them in a ProviderSubscriptionResponseDTO.
     * @param subscriptionId the unique identifier of the subscription to retrieve, which should be provided by the payment provider when the subscription was created
     * @return a ProviderSubscriptionResponseDTO containing details about the subscription, such as its status, plan information, and any relevant data from the payment provider
     */
    ProviderSubscriptionResponseDTO getSubscription(String subscriptionId);

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
     * Retrieves the details of a payment based on the provided payment ID. This method should interact with the payment provider's API to fetch the payment details and return them in a ProviderPaymentResponseDTO.
     * @param paymentId the unique identifier of the payment to retrieve, which should be provided by the payment provider when the payment was processed
     * @return a ProviderPaymentResponseDTO containing details about the payment, such as its amount, status, currency, approval date, and any relevant data from the payment provider
     */
    ProviderPaymentResponseDTO getPayment(String paymentId);

    /**
     * Validates the webhook request by verifying the signature and ensuring that the request is legitimate. This method should check the provided signature against the expected value based on the payment provider's specifications, and also verify the request ID and data ID to ensure that the webhook is valid and has not been tampered with.
     * @param signature the signature provided in the webhook request, which should be compared against the expected signature generated using the payment provider's secret key and the request payload
     * @param requestId the unique identifier of the webhook request, which can be used to track and log the request for auditing purposes
     * @param dataId the unique identifier of the data associated with the webhook event, which can be used to correlate the webhook event with specific actions or records in the system
     */
    void validateWebhook(String signature, String requestId, String dataId);
}

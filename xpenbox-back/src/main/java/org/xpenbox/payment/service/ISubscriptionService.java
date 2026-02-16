package org.xpenbox.payment.service;

import org.xpenbox.payment.dto.PreApprovalSubscriptionRequestDTO;
import org.xpenbox.payment.dto.PreApprovalSubscriptionResponseDTO;
import org.xpenbox.payment.dto.SubscriptionResponseDTO;

/**
 * The ISubscriptionService interface defines the contract for subscription-related operations in the application. It serves as an abstraction layer that allows for different implementations of subscription services, enabling flexibility and scalability in handling various subscription providers and methods. This interface can include methods for creating subscriptions, processing payments, handling refunds, and managing webhooks, among other subscription-related functionalities. Implementing this interface allows for a consistent way to interact with different subscription providers while keeping the business logic separate from the specific implementation details of each provider.
 */
public interface ISubscriptionService {

    /**
     * Creates a free subscription for a user based on their email. This method should interact with the subscription provider's API to create a free subscription, which may involve setting up a trial period or a subscription plan with no cost. The method should return a SubscriptionResponseDTO containing details about the created free subscription, such as its ID, status, and any relevant information from the subscription provider.
     * @param userEmail the email of the user for whom the free subscription is being created, which may be used for associating the subscription with the user in the subscription provider's system
     * @return a SubscriptionResponseDTO containing details about the created free subscription, including its ID, status, and any relevant information from the subscription provider
     */
    SubscriptionResponseDTO createFreeSubscription(String userEmail);

    /**
     * Retrieves the active subscription for a given user email. This method should interact with the subscription provider's API to fetch the current active subscription associated with the provided user email. The method should return a SubscriptionResponseDTO containing details about the active subscription, such as its ID, status, plan details, and any relevant information from the subscription provider.
     * @param userEmail the email of the user for whom the active subscription is being retrieved, which may be used for identifying the subscription in the subscription provider's system and ensuring that the correct subscription information is returned
     * @return a SubscriptionResponseDTO containing details about the active subscription for the specified user email, including its ID, status, plan details, and any relevant information from the subscription provider
     */
    SubscriptionResponseDTO getActiveSubscription(String userEmail);

    /**
     * Creates a pre-approval subscription based on the provided request and user email. This method should interact with the subscription provider's API to create a pre-approval subscription, which is typically used for recurring payments or subscriptions. The method should return a PreApprovalSubscriptionResponseDTO containing details about the created pre-approval subscription, such as its ID, status, and any relevant URLs for managing the subscription.
     * @param request the PreApprovalSubscriptionRequestDTO containing the necessary information to create the pre-approval subscription, such as the plan details, payment amount, and frequency
     * @param userEmail the email of the user for whom the pre-approval subscription is being created, which may be used for associating the subscription with the user in the subscription provider's system
     * @return a PreApprovalSubscriptionResponseDTO containing details about the created pre-approval subscription, including its ID, status, and any relevant URLs for managing the subscription
     */
    PreApprovalSubscriptionResponseDTO createPreApprovalSubscription(PreApprovalSubscriptionRequestDTO request, String userEmail);

    /**
     * Cancels an existing subscription based on the provided resource code and user email. This method should interact with the subscription provider's API to cancel the subscription, which may involve updating the subscription status or deleting it from the provider's system. The method should return a PreApprovalSubscriptionResponseDTO containing details about the canceled subscription, such as its ID, cancellation status, and any relevant information from the subscription provider.
     * @param resourceCode the unique resource code of the subscription to be canceled, which should be provided by the subscription provider when the subscription was created and may be used to identify the subscription in the provider's system
     * @param userEmail the email of the user associated with the subscription being canceled, which may be used for verifying the user's identity and ensuring that the correct subscription is canceled in the subscription provider's system
     */
    void cancelSubscription(String resourceCode, String userEmail);
}

package org.xpenbox.payment.service;

import org.xpenbox.payment.dto.PreApprovalSubscriptionRequestDTO;
import org.xpenbox.payment.dto.PreApprovalSubscriptionResponseDTO;
import org.xpenbox.payment.dto.SubscriptionResponseDTO;
import org.xpenbox.payment.entity.Subscription;

/**
 * The ISubscriptionService interface defines the contract for subscription-related operations in the application. It serves as an abstraction layer that allows for different implementations of subscription services, enabling flexibility and scalability in handling various subscription providers and methods. This interface can include methods for creating subscriptions, processing payments, handling refunds, and managing webhooks, among other subscription-related functionalities. Implementing this interface allows for a consistent way to interact with different subscription providers while keeping the business logic separate from the specific implementation details of each provider.
 */
public interface ISubscriptionService {

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
     * Cancels the active subscription for a given user email. This method should interact with the subscription provider's API to cancel the current active subscription associated with the provided user email. The method should ensure that the subscription is properly cancelled in the subscription provider's system and that any relevant information about the cancellation is returned or logged as needed.
     * @param userEmail the email of the user for whom the active subscription is being cancelled, which may be used for identifying the subscription in the subscription provider's system and ensuring that the correct subscription is cancelled
     */
    void cancelActiveSubscription(String userEmail);

    /**
     * Finds an active subscription for the given user ID. This method queries the subscription repository to find a subscription with the status of ACTIVE for the specified user. If an active subscription is found, it is returned; otherwise, null is returned.
     * @param userId The ID of the user for whom to find the active subscription.
     * @return The Subscription entity representing the active subscription for the user, or null if no active subscription is found.
     */
    Subscription findActiveSubscription(Long userId);
}

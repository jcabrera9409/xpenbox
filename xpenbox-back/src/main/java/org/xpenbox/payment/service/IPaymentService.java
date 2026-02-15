package org.xpenbox.payment.service;

import org.xpenbox.payment.dto.PreApprovalSubscriptionRequestDTO;
import org.xpenbox.payment.dto.PreApprovalSubscriptionResponseDTO;

/**
 * The IPaymentService interface defines the contract for payment-related operations in the application. It serves as an abstraction layer that allows for different implementations of payment services, enabling flexibility and scalability in handling various payment providers and methods. This interface can include methods for creating subscriptions, processing payments, handling refunds, and managing webhooks, among other payment-related functionalities. Implementing this interface allows for a consistent way to interact with different payment providers while keeping the business logic separate from the specific implementation details of each provider.
 */
public interface IPaymentService {

    /**
     * Creates a pre-approval subscription based on the provided request and user email. This method should interact with the payment provider's API to create a pre-approval subscription, which is typically used for recurring payments or subscriptions. The method should return a PreApprovalSubscriptionResponseDTO containing details about the created pre-approval subscription, such as its ID, status, and any relevant URLs for managing the subscription.
     * @param request the PreApprovalSubscriptionRequestDTO containing the necessary information to create the pre-approval subscription, such as the plan details, payment amount, and frequency
     * @param userEmail the email of the user for whom the pre-approval subscription is being created, which may be used for associating the subscription with the user in the payment provider's system
     * @return a PreApprovalSubscriptionResponseDTO containing details about the created pre-approval subscription, including its ID, status, and any relevant URLs for managing the subscription
     */
    PreApprovalSubscriptionResponseDTO createPreApprovalSubscription(PreApprovalSubscriptionRequestDTO request, String userEmail);
}

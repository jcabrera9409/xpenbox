package org.xpenbox.payment.provider.mapper;

import org.jboss.logging.Logger;
import org.xpenbox.payment.entity.Plan;
import org.xpenbox.payment.enums.PaymentProviderType;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionRequestDTO;
import org.xpenbox.user.entity.User;

import jakarta.inject.Singleton;

/**
 * ProviderMapper is a utility class responsible for mapping internal entities (such as Plan and User) to Data Transfer Objects (DTOs) that are used for communication with payment providers. It provides methods to convert Plan and User information into a format that can be understood by the payment provider's API when creating subscription plans.
 */
@Singleton
public class ProviderMapper {
    private static final Logger LOG = Logger.getLogger(ProviderMapper.class);

    /**
     * Maps a Plan entity and User information to a ProviderPlanRequestDTO, which is used to create a subscription plan with a payment provider. This method extracts relevant information from the Plan and User entities, such as the user's email, plan name, price, currency, billing cycle, and payment provider type, and constructs a ProviderPlanRequestDTO that can be sent to the payment provider's API.
     * @param plan The Plan entity containing details about the subscription plan, such as name, price, currency, and billing cycle.
     * @param user The User entity containing details about the user subscribing to the plan, such as email and ID.
     * @param paymentProviderType The type of payment provider for which the subscription plan is being created (e.g., MercadoPago, Stripe).
     * @return a ProviderPlanRequestDTO containing the necessary information to create a subscription plan with the specified payment provider.
     */
    public ProviderSubscriptionRequestDTO toSubscriptionPlanRequestDTO(Plan plan, User user, PaymentProviderType paymentProviderType) {
        LOG.infof("Mapping Plan entity to SubscriptionPlanRequestDTO for user %s and plan %s", user.getEmail(), plan.getName());

        return new ProviderSubscriptionRequestDTO(
            user.id,
            user.getEmail(),
            plan.getName(),
            plan.getPrice(),
            plan.getCurrency(),
            plan.getBillingCycle(),
            paymentProviderType
        );
    }
}

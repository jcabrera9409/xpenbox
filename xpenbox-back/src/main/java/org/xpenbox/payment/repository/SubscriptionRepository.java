package org.xpenbox.payment.repository;

import java.util.Optional;

import org.jboss.logging.Logger;
import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.payment.entity.Subscription;
import org.xpenbox.payment.entity.Subscription.SubscriptionStatus;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for managing Subscription entities.
 */
@ApplicationScoped
public class SubscriptionRepository extends GenericRepository<Subscription> {
    private static final Logger LOG = Logger.getLogger(SubscriptionRepository.class);

    /**
     * Finds a subscription by user ID and subscription status. This method queries the database for a subscription that matches the provided user ID and status, allowing for retrieval of active, canceled, or other subscription states for a specific user.
     * @param userId the unique identifier of the user for whom the subscription is being queried, which should correspond to the user ID associated with the subscription records in the database
     * @param status the status of the subscription to filter by, which can be used to retrieve subscriptions that are active, canceled, or in other states as defined in the SubscriptionStatus enum
     * @return an Optional containing the Subscription if found, or an empty Optional if no matching subscription is found for the given user ID and status
     */
    public Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status) {
        LOG.debugf("Finding subscription with user ID: %s and status: %s", userId, status);
        return find("user.id = :userId and status = :status", 
            Parameters.with("userId", userId).and("status", status)
        ).firstResultOptional();
    }

    /**
     * Finds a subscription by provider subscription ID and provider type. This method queries the database for a subscription that matches the provided provider subscription ID and provider type, allowing for retrieval of subscriptions based on the identifiers provided by the payment providers.
     * @param providerSubscriptionId the unique identifier of the subscription as provided by the payment provider, which should correspond to the provider subscription ID stored in the subscription records in the database
     * @param providerType the type of the payment provider, which can be used to filter subscriptions based on the specific payment provider they are associated with, such as Stripe, PayPal, MercadoPago, or others
     * @return an Optional containing the Subscription if found, or an empty Optional if no matching subscription is found for the given provider subscription ID and provider type
     */
    public Optional<Subscription> findByProviderSubscriptionIdAndProvider(String providerSubscriptionId, String providerType) {
        LOG.debugf("Finding subscription with provider subscription ID: %s and provider type: %s", providerSubscriptionId, providerType);
        return find("providerSubscriptionId = :providerSubscriptionId and provider = :provider", 
            Parameters.with("providerSubscriptionId", providerSubscriptionId).and("provider", providerType)
        ).firstResultOptional();
    }
}

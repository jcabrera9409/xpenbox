package org.xpenbox.payment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.payment.entity.Subscription;
import org.xpenbox.payment.entity.Subscription.SubscriptionStatus;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;

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

    /**
     * Finds a subscription by provider subscription ID and provider type, acquiring a pessimistic write lock (SELECT FOR UPDATE).
     * Use this method when processing webhooks to prevent race conditions: only one transaction can hold the lock at a time,
     * ensuring the duplicate-payment check and subscription update are atomic.
     * @param providerSubscriptionId the unique identifier of the subscription from the payment provider
     * @param providerType the name of the payment provider
     * @return an Optional containing the locked Subscription if found, or empty if not found
     */
    public Optional<Subscription> findByProviderSubscriptionIdAndProviderWithLock(String providerSubscriptionId, String providerType) {
        LOG.debugf("Finding subscription with provider subscription ID: %s and provider type: %s (with PESSIMISTIC_WRITE lock)", providerSubscriptionId, providerType);
        return find("providerSubscriptionId = :providerSubscriptionId and provider = :provider", 
                        Parameters.with("providerSubscriptionId", providerSubscriptionId).and("provider", providerType))
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .firstResultOptional();
    }

    /**
     * Finds all subscriptions by status, plan ID, and end date before a specified date. This method queries the database for all subscriptions that match the provided subscription status, plan ID, and have an end date before the specified date, allowing for retrieval of subscriptions based on their current state, the specific subscription plan they are associated with, and their expiration status.
     * @param status the status of the subscriptions to filter by, which can be used to retrieve subscriptions that are active, canceled, or in other states as defined in the SubscriptionStatus enum
     * @param planId the unique identifier of the subscription plan, which should correspond to the plan ID stored in the subscription records in the database, allowing for retrieval of subscriptions that are associated with a specific subscription plan
     * @param beforeEndDate the date and time to filter subscriptions that have an end date before this value, which can be used to retrieve subscriptions that are nearing expiration or have already expired based on their end date
     * @return a list of Subscription entities that match the provided status, plan ID, and end date criteria, which may be empty if no subscriptions are found that meet the criteria
     */
    public List<Subscription> findAllSubscriptionsByStatusAndPlanIdAndBeforeEndDate(SubscriptionStatus status, Long planId, LocalDateTime beforeEndDate) {
        LOG.debugf("Finding all subscriptions with status: %s, plan ID: %s, and end date before: %s", status, planId, beforeEndDate);
        return list("status = :status and plan.id = :planId and endDate < :beforeEndDate", 
            Parameters.with("status", status).and("planId", planId).and("beforeEndDate", beforeEndDate)
        );
    }
}

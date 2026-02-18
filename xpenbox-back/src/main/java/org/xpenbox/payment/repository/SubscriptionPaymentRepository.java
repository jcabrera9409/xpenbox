package org.xpenbox.payment.repository;

import java.util.Optional;

import org.jboss.logging.Logger;
import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.payment.entity.SubscriptionPayment;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for managing SubscriptionPayment entities.
 */
@ApplicationScoped
public class SubscriptionPaymentRepository extends GenericRepository<SubscriptionPayment> {
    private static final Logger LOG = Logger.getLogger(SubscriptionPaymentRepository.class);

    /**
     * Finds a SubscriptionPayment entity based on the subscription ID, provider payment ID, and provider name. This method is useful for retrieving specific payment records associated with a subscription and a particular payment provider, allowing for efficient querying of payment data in the context of subscription management.
     * @param subscriptionId the ID of the subscription associated with the payment
     * @param providerPaymentId the unique identifier of the payment from the payment provider
     * @param provider the name of the payment provider (e.g., "MERCADOPAGO", "STRIPE") to filter the payment records
     * @return an Optional containing the SubscriptionPayment entity that matches the provided criteria, or an empty Optional if no matching record is found
     */
    public Optional<SubscriptionPayment> findBySubscriptionIdAndProviderPaymentIdAndProvider(Long subscriptionId, String providerPaymentId, String provider) {
        LOG.debugf("Finding SubscriptionPayment with subscriptionId: %d, providerPaymentId: %s, provider: %s", subscriptionId, providerPaymentId, provider);
        return find("subscription.id = :subscriptionId AND providerPaymentId = :providerPaymentId AND provider = :provider",
                    Parameters.with("subscriptionId", subscriptionId)
                              .and("providerPaymentId", providerPaymentId)
                              .and("provider", provider))
               .firstResultOptional();
    }
}

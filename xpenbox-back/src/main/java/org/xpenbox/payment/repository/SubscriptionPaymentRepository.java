package org.xpenbox.payment.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.payment.entity.SubscriptionPayment;
import org.xpenbox.payment.entity.SubscriptionPayment.PaymentStatus;

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

    /**
     * Finds a SubscriptionPayment entity based on the subscription ID, provider name, next billing date, and payment status. This method allows for querying payment records that are associated with a specific subscription and provider, while also filtering by the expected next billing date and the current status of the payment, which can be useful for managing recurring payments and subscription renewals.
     * @param subscriptionId the ID of the subscription associated with the payment
     * @param provider the name of the payment provider (e.g., "MERCADOPAGO", "STRIPE") to filter the payment records
     * @param from the start of the period for the subscription payment
     * @param to the end of the period for the subscription payment
     * @param status the current status of the subscription payment
     * @return an Optional containing the SubscriptionPayment entity that matches the provided criteria, or an empty Optional if no matching record is found
     */
    public Optional<SubscriptionPayment> findBySubscriptionIdAndProviderAndPaymentDateInPeriodAndStatus(Long subscriptionId, String provider, LocalDateTime from, LocalDateTime to, PaymentStatus status) {
        LOG.debugf("Finding SubscriptionPayment with subscriptionId: %d, provider: %s, paymentDate between: %s and %s, status: %s", subscriptionId, provider, from, to, status);
        return find("subscription.id = :subscriptionId AND provider = :provider AND paymentDate >= :from AND paymentDate < :to AND status = :status ORDER BY paymentDate DESC",
                    Parameters.with("subscriptionId", subscriptionId)
                              .and("provider", provider)
                              .and("from", from)
                              .and("to", to)
                              .and("status", status))
               .firstResultOptional();
    }
}

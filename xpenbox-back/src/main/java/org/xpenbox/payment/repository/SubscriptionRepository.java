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

    public Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status) {
        LOG.debugf("Finding subscription with user ID: %s and status: %s", userId, status);
        return find("user.id = :userId and status = :status", 
            Parameters.with("userId", userId).and("status", status)
        ).firstResultOptional();
    }
}

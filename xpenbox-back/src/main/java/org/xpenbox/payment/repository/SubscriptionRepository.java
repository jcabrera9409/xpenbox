package org.xpenbox.payment.repository;

import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.payment.entity.Subscription;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for managing Subscription entities.
 */
@ApplicationScoped
public class SubscriptionRepository extends GenericRepository<Subscription> {
    
}

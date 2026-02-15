package org.xpenbox.payment.repository;

import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.payment.entity.SubscriptionPayment;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for managing SubscriptionPayment entities.
 */
@ApplicationScoped
public class SubscriptionPaymentRepository extends GenericRepository<SubscriptionPayment> {
    
}

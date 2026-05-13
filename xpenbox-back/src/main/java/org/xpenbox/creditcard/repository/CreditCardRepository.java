package org.xpenbox.creditcard.repository;

import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.creditcard.entity.CreditCard;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for CreditCard entity operations.
 */
@ApplicationScoped
public class CreditCardRepository extends GenericRepository<CreditCard> {
    private static final Logger LOG = Logger.getLogger(CreditCardRepository.class);

    public List<CreditCard> findAllActiveByBillingDayOrPaymentDay(List<Byte> billingDays, List<Byte> paymentDays) {
        LOG.infof("Finding all active credit cards with billing day in %s or payment day in %s", billingDays, paymentDays);
        return list("state = true AND (billingDay IN :billingDays OR paymentDay IN :paymentDays)", 
            Parameters.with("billingDays", billingDays).and("paymentDays", paymentDays)
        );
    }
}
    
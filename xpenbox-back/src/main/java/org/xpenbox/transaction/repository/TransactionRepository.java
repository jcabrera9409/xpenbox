package org.xpenbox.transaction.repository;

import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.transaction.entity.Transaction;
import org.xpenbox.transaction.entity.Transaction.TransactionType;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for Transaction entities.
 */
@ApplicationScoped
public class TransactionRepository extends GenericRepository<Transaction> {
    private static final Logger LOG = Logger.getLogger(TransactionRepository.class);

    /**
     * Find transactions by user ID and transaction type.
     * @param incomeId the ID of the income
     * @param userId the ID of the user
     * @param type the type of transaction
     * @return a list of transactions matching the criteria
     */
    public List<Transaction> findByIncomeIdAndUserIdAndType(Long incomeId, Long userId, TransactionType transactionType) {
        LOG.debugf("Fetching transactions for Income ID: %d, User ID: %d with type: %s", incomeId, userId, transactionType);
        return list("income.id = :incomeId and user.id = :userId and transactionType = :transactionType", 
                Parameters.with("incomeId", incomeId).and("userId", userId).and("transactionType", transactionType));
    }
}

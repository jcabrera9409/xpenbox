package org.xpenbox.transaction.repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.transaction.dto.TransactionFilterDTO;
import org.xpenbox.transaction.entity.Transaction;
import org.xpenbox.transaction.entity.Transaction.TransactionType;
import org.xpenbox.user.entity.User;

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
    
    /**
     * Find transactions based on filter criteria and user.
     * @param filterDTO the filter criteria
     * @param user the user
     * @return a list of transactions matching the filter criteria
     */
    public List<Transaction> findByFilter(TransactionFilterDTO filterDTO, User user) {
        LOG.debugf("Filtering transactions with filterDTO: %s for user: %s", filterDTO, user);
        if (filterDTO != null) {
            String filterQuery = buildFilterQuery(filterDTO, true);
            Parameters params = buildParameters(filterDTO, user);

            if (filterDTO.pageNumber() != null && filterDTO.pageSize() != null) {
                int pageNumber = filterDTO.pageNumber();
                int pageSize = filterDTO.pageSize();
                LOG.debugf("Applying pagination - Page Number: %d, Page Size: %d", pageNumber, pageSize);
                return find(filterQuery, params)
                        .page(pageNumber, pageSize)
                        .list();
            }

            return list(filterQuery, params);
        }
        return listAll();
    }

    /**
     * Count transactions based on filter criteria and user.
     * @param filterDTO the filter criteria
     * @param user the user
     * @return the count of transactions matching the filter criteria
     */
    public Integer countByFilter(TransactionFilterDTO filterDTO, User user) {
        LOG.debugf("Counting transactions with filterDTO: %s for user: %s", filterDTO, user);
        if (filterDTO != null) {
            String filterQuery = buildFilterQuery(filterDTO, false);
            Parameters params = buildParameters(filterDTO, user);

            LOG.debugf("Counting with filterQuery: %s and params: %s", filterQuery, params);
            return Math.toIntExact(count(filterQuery, params));
        }

        LOG.debug("No filterDTO provided, counting all transactions.");
        return Math.toIntExact(count());
    }

    /**
     * Build query parameters based on filter criteria and user.
     * @param filterDTO the filter criteria
     * @param user the user
     * @return the constructed parameters
     */
    private Parameters buildParameters(TransactionFilterDTO filterDTO, User user) {
        LOG.debugf("Building parameters for filterDTO: %s and user: %s", filterDTO, user);

        Parameters params = Parameters.with("userId", user.id);
        
        if (filterDTO.resourceCode() != null) {
            params.and("resourceCode", filterDTO.resourceCode());
        }
        
        if (filterDTO.transactionType() != null) {
            params.and("transactionType", filterDTO.transactionType());
        }

        if (filterDTO.description() != null) {
            params.and("description", filterDTO.description());
        }

        if (filterDTO.transactionDateTimestampFrom() != null && filterDTO.transactionDateTimestampTo() != null) {
            LocalDateTime from = Instant.ofEpochMilli(filterDTO.transactionDateTimestampFrom()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime to = Instant.ofEpochMilli(filterDTO.transactionDateTimestampTo()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            params.and("transactionDateFrom", from);
            params.and("transactionDateTo", to);
        }

        if (filterDTO.categoryResourceCode() != null) {
            params.and("categoryResourceCode", filterDTO.categoryResourceCode());
        }

        if (filterDTO.incomeResourceCode() != null) {
            params.and("incomeResourceCode", filterDTO.incomeResourceCode());
        }

        if (filterDTO.accountResourceCode() != null) {
            params.and("accountResourceCode", filterDTO.accountResourceCode());
        }

        if (filterDTO.creditCardResourceCode() != null) {
            params.and("creditCardResourceCode", filterDTO.creditCardResourceCode());
        }

        return params;
    }

    /**
     * Build filter query string based on filter criteria.
     * @param filterDTO the filter criteria
     * @param orderByDateDesc whether to order by date descending
     * @return the constructed filter query string
     */
    private String buildFilterQuery(TransactionFilterDTO filterDTO, boolean orderByDateDesc) {
        LOG.debugf("Building filter query for filterDTO: %s", filterDTO);

        StringBuilder queryBuilder = new StringBuilder("user.id = :userId");
        
        if (filterDTO.resourceCode() != null) {
            queryBuilder.append(" and resourceCode = :resourceCode");
        }

        if (filterDTO.transactionType() != null) {
            queryBuilder.append(" and transactionType = :transactionType");
        }
        
        if (filterDTO.description() != null) {
            queryBuilder.append(" and lower(description) like lower(concat('%', :description, '%'))");
        }

        if (filterDTO.transactionDateTimestampFrom() != null && filterDTO.transactionDateTimestampTo() != null) {
            queryBuilder.append(" and transactionDate between :transactionDateFrom and :transactionDateTo");
        }

        if (filterDTO.categoryResourceCode() != null) {
            queryBuilder.append(" and category.resourceCode = :categoryResourceCode");
        }

        if (filterDTO.incomeResourceCode() != null) {
            queryBuilder.append(" and income.resourceCode = :incomeResourceCode");
        }

        if (filterDTO.accountResourceCode() != null) {
            queryBuilder.append(" and (account.resourceCode = :accountResourceCode or destinationAccount.resourceCode = :accountResourceCode)");
        }

        if (filterDTO.creditCardResourceCode() != null) {
            queryBuilder.append(" and creditCard.resourceCode = :creditCardResourceCode");
        }

        if (orderByDateDesc) {
            queryBuilder.append(" order by transactionDate DESC");
        }

        return queryBuilder.toString();
    }
}

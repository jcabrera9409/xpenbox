package org.xpenbox.transaction.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Find assigned amounts by income IDs, user ID, and transaction type.
     * @param incomeIds the list of income IDs
     * @param userId the user ID
     * @param transactionType the type of transaction
     * @return a map of income ID to assigned amount
     */
    public Map<Long, BigDecimal> findAssignedAmountByIncomeIdsAndUserIdAndTransactionType(List<Long> incomeIds, Long userId, TransactionType transactionType) {
        LOG.debugf("Calculating assigned amounts for Income IDs: %s, User ID: %d with type: %s", incomeIds, userId, transactionType);
        String query = "SELECT income.id, SUM(amount) FROM Transaction "
                     + "WHERE income.id IN :incomeIds AND user.id = :userId AND transactionType = :transactionType "
                     + "GROUP BY income.id";
        
        List<Object[]> results = getEntityManager().createQuery(query, Object[].class)
                .setParameter("incomeIds", incomeIds)
                .setParameter("userId", userId)
                .setParameter("transactionType", transactionType)
                .getResultList();
        
        Map<Long, BigDecimal> assignedAmounts = results.stream()
                .collect(Collectors.toMap(
                    row -> (Long) row[0],
                    row -> (BigDecimal) row[1]
                ));
        
        LOG.debugf("Assigned amounts calculated: %s", assignedAmounts);
        return assignedAmounts;
    }

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
            // Use explicit LEFT JOIN query when filtering by accountResourceCode
            if (filterDTO.accountResourceCode() != null) {
                return findByFilterWithAccountJoin(filterDTO, user);
            }
            
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
            // Use explicit LEFT JOIN query when filtering by accountResourceCode
            if (filterDTO.accountResourceCode() != null) {
                return countByFilterWithAccountJoin(filterDTO, user);
            }
            
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
            // This condition is handled by findByFilterWithAccountJoin/countByFilterWithAccountJoin
            // to avoid INNER JOIN issues with nullable destinationAccount
            queryBuilder.append(" and account.resourceCode = :accountResourceCode");
        }

        if (filterDTO.creditCardResourceCode() != null) {
            queryBuilder.append(" and creditCard.resourceCode = :creditCardResourceCode");
        }

        if (orderByDateDesc) {
            queryBuilder.append(" order by transactionDate DESC");
        }

        return queryBuilder.toString();
    }

    /**
     * Find transactions by filter with explicit LEFT JOIN for destinationAccount.
     * This method is used when filtering by accountResourceCode to avoid INNER JOIN issues.
     * @param filterDTO the filter criteria
     * @param user the user
     * @return a list of transactions matching the filter criteria
     */
    private List<Transaction> findByFilterWithAccountJoin(TransactionFilterDTO filterDTO, User user) {
        StringBuilder hql = new StringBuilder("SELECT t FROM Transaction t ");
        hql.append("LEFT JOIN t.destinationAccount da ");
        hql.append("WHERE t.user.id = :userId ");
        hql.append("AND (t.account.resourceCode = :accountResourceCode OR da.resourceCode = :accountResourceCode) ");
        
        appendCommonFilters(hql, filterDTO);
        hql.append(" ORDER BY t.transactionDate DESC");
        
        var query = getEntityManager().createQuery(hql.toString(), Transaction.class);
        setCommonParameters(query, filterDTO, user);
        
        if (filterDTO.pageNumber() != null && filterDTO.pageSize() != null) {
            query.setFirstResult(filterDTO.pageNumber() * filterDTO.pageSize());
            query.setMaxResults(filterDTO.pageSize());
        }
        
        return query.getResultList();
    }

    /**
     * Count transactions by filter with explicit LEFT JOIN for destinationAccount.
     * This method is used when filtering by accountResourceCode to avoid INNER JOIN issues.
     * @param filterDTO the filter criteria
     * @param user the user
     * @return the count of transactions matching the filter criteria
     */
    private Integer countByFilterWithAccountJoin(TransactionFilterDTO filterDTO, User user) {
        StringBuilder hql = new StringBuilder("SELECT COUNT(t) FROM Transaction t ");
        hql.append("LEFT JOIN t.destinationAccount da ");
        hql.append("WHERE t.user.id = :userId ");
        hql.append("AND (t.account.resourceCode = :accountResourceCode OR da.resourceCode = :accountResourceCode) ");
        
        appendCommonFilters(hql, filterDTO);
        
        var query = getEntityManager().createQuery(hql.toString(), Long.class);
        setCommonParameters(query, filterDTO, user);
        
        return Math.toIntExact(query.getSingleResult());
    }

    /**
     * Append common filter conditions to HQL query.
     * @param hql the query builder
     * @param filterDTO the filter criteria
     */
    private void appendCommonFilters(StringBuilder hql, TransactionFilterDTO filterDTO) {
        if (filterDTO.resourceCode() != null) {
            hql.append("AND t.resourceCode = :resourceCode ");
        }
        if (filterDTO.transactionType() != null) {
            hql.append("AND t.transactionType = :transactionType ");
        }
        if (filterDTO.description() != null) {
            hql.append("AND LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')) ");
        }
        if (filterDTO.transactionDateTimestampFrom() != null && filterDTO.transactionDateTimestampTo() != null) {
            hql.append("AND t.transactionDate BETWEEN :transactionDateFrom AND :transactionDateTo ");
        }
        if (filterDTO.categoryResourceCode() != null) {
            hql.append("AND t.category.resourceCode = :categoryResourceCode ");
        }
        if (filterDTO.incomeResourceCode() != null) {
            hql.append("AND t.income.resourceCode = :incomeResourceCode ");
        }
        if (filterDTO.creditCardResourceCode() != null) {
            hql.append("AND t.creditCard.resourceCode = :creditCardResourceCode ");
        }
    }

    /**
     * Set common parameters for HQL query.
     * @param query the query
     * @param filterDTO the filter criteria
     * @param user the user
     */
    private void setCommonParameters(jakarta.persistence.Query query, TransactionFilterDTO filterDTO, User user) {
        query.setParameter("userId", user.id);
        query.setParameter("accountResourceCode", filterDTO.accountResourceCode());
        
        if (filterDTO.resourceCode() != null) {
            query.setParameter("resourceCode", filterDTO.resourceCode());
        }
        if (filterDTO.transactionType() != null) {
            query.setParameter("transactionType", filterDTO.transactionType());
        }
        if (filterDTO.description() != null) {
            query.setParameter("description", filterDTO.description());
        }
        if (filterDTO.transactionDateTimestampFrom() != null && filterDTO.transactionDateTimestampTo() != null) {
            LocalDateTime from = Instant.ofEpochMilli(filterDTO.transactionDateTimestampFrom()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime to = Instant.ofEpochMilli(filterDTO.transactionDateTimestampTo()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            query.setParameter("transactionDateFrom", from);
            query.setParameter("transactionDateTo", to);
        }
        if (filterDTO.categoryResourceCode() != null) {
            query.setParameter("categoryResourceCode", filterDTO.categoryResourceCode());
        }
        if (filterDTO.incomeResourceCode() != null) {
            query.setParameter("incomeResourceCode", filterDTO.incomeResourceCode());
        }
        if (filterDTO.creditCardResourceCode() != null) {
            query.setParameter("creditCardResourceCode", filterDTO.creditCardResourceCode());
        }
    }
}

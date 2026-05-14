package org.xpenbox.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.xpenbox.user.entity.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for User entity.
 */
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    private static final Logger LOG = Logger.getLogger(UserRepository.class);

    /**
     * Finds a user by their email.
     *
     * @param email the email of the user
     * @return an Optional containing the User if found, or empty if not found
     */
    public Optional<User> findByEmail(String email) {
        LOG.infof("Finding user by email: %s", email);
        return find("email", email).firstResultOptional();
    }

    public List<User> findAllUsersWithoutTransactionsAndDates(LocalDateTime startDate, LocalDateTime endDate) {
        LOG.infof("Finding all users without transactions between %s and %s", startDate, endDate);
        return list("FROM User u LEFT JOIN Transaction t ON t.user = u AND t.transactionDate BETWEEN :startDate AND :endDate WHERE t.id IS NULL",
                    Parameters.with("startDate", startDate).and("endDate", endDate));
    }
}

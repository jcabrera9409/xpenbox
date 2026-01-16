package org.xpenbox.account.repository;

import java.util.List;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.xpenbox.account.entity.Account;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for Account entity.
 */
@ApplicationScoped
public class AccountRepository implements PanacheRepository<Account> {
    private static final Logger LOG = Logger.getLogger(AccountRepository.class);

    /**
     * Find an account by its resource code.
     * @param resourceCode the resource code of the account
     * @return an Optional containing the Account if found, or empty if not found
     */
    public Optional<Account> findByResourceCode(String resourceCode) {
        LOG.infof("Finding account by resource code: %s", resourceCode);
        return find("resourceCode", resourceCode).firstResultOptional();
    }

    /**
     * Find all accounts associated with a specific user ID.
     * @param userId the ID of the user
     * @return a list of Accounts associated with the user ID
     */
    public List<Account> findAllById(Long userId) {
        LOG.infof("Finding all accounts for user id: %d", userId);
        return list("user.id", userId);
    }
}

package org.xpenbox.user.repository;

import java.util.Optional;

import org.jboss.logging.Logger;
import org.xpenbox.user.entity.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
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
}

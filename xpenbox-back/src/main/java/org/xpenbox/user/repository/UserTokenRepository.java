package org.xpenbox.user.repository;

import org.jboss.logging.Logger;
import org.xpenbox.user.entity.UserToken;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserTokenRepository implements PanacheRepository<UserToken> {
    private static final Logger LOG = Logger.getLogger(UserTokenRepository.class);

    /**
     * Finds a UserToken by user ID and token value.
     *
     * @param userId the ID of the user
     * @param token the token value
     * @return the UserToken if found, or null if not found
     */
    public UserToken findByUserIdAndToken(Long userId, String token) {
        LOG.infof("Finding UserToken for userId: %d and token: %s", userId, token);
        return find("user.id = :userId and token = :token", 
            Parameters.with("userId", userId).and("token", token)).firstResult();
    }
    
}

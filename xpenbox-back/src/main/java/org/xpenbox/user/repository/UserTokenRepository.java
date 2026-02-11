package org.xpenbox.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.xpenbox.user.entity.UserToken;
import org.xpenbox.user.entity.UserToken.UserTokenType;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserTokenRepository implements PanacheRepository<UserToken> {
    private static final Logger LOG = Logger.getLogger(UserTokenRepository.class);

    /**
     * Finds an active UserToken by user ID and token type.
     *
     * @param userId the ID of the user
     * @param tokenType the type of the token
     * @return the active UserToken if found, or null if not found
     */
    public UserToken findActiveTokenByUserIdAndType(Long userId, UserTokenType tokenType) {
        return find("user.id = :userId and tokenType = :tokenType and expiresAt > :now", 
            Parameters.with("userId", userId)
                    .and("tokenType", tokenType)
                    .and("now", LocalDateTime.now()))
            .firstResult();
    }

    /**
     * Finds a UserToken by its token value.
     *
     * @param token the token value
     * @return the UserToken if found, or null if not found
     */
    public Optional<UserToken> findByToken(String token) {
        LOG.infof("Finding UserToken for token: %s", token);
        return find("token = :token", 
            Parameters.with("token", token)).firstResultOptional();
    }

    /**
     * Deletes UserTokens by user ID and token type.
     * @param userId the ID of the user
     * @param tokenType the type of the token to delete
     */
    public void deleteByUserIdAndUserTokenType(Long userId, UserTokenType tokenType) {
        LOG.infof("Deleting UserTokens for userId: %d and tokenType: %s", userId, tokenType);
        delete("user.id = :userId and tokenType = :tokenType", 
            Parameters.with("userId", userId).and("tokenType", tokenType));
    }
    
}

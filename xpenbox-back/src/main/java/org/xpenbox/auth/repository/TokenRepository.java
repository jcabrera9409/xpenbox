package org.xpenbox.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.xpenbox.auth.entity.Token;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Repository for managing Token entities.
 */
@ApplicationScoped
public class TokenRepository implements PanacheRepository<Token> {
    private static final Logger LOG = Logger.getLogger(TokenRepository.class);

    /**
     * Finds a valid (not revoked and not expired) refresh token by its value.
     *
     * @param refreshToken the refresh token value to search for
     * @return an Optional containing the Token if found and valid, otherwise empty
     */
    public Optional<Token> findValidRefreshToken(String refreshToken) {
        LOG.debugf("Finding valid refresh token: %s", refreshToken);
        return find("""
            refreshToken = :refreshToken and 
            revoked = false and 
            (refreshTokenExpiresAt is null or refreshTokenExpiresAt > :refreshTokenExpiresAt)
            """, Parameters.with("refreshToken", refreshToken).and("refreshTokenExpiresAt", LocalDateTime.now()))
                .firstResultOptional();
    }
    
    /**
     * Revokes all tokens associated with a specific user ID.
     *
     * @param userId the ID of the user whose tokens should be revoked
     */
    @Transactional
    public void revokeAllByUserId(Long userId) {
        LOG.debugf("Revoking all tokens for user ID: %d", userId);
        update("revoked = true where user.id = :userId", Parameters.with("userId", userId));
    }
}

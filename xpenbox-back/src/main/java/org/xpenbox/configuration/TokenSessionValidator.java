package org.xpenbox.configuration;

import java.time.LocalDateTime;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.xpenbox.authorization.entity.Token;
import org.xpenbox.authorization.repository.TokenRepository;

import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;

@ApplicationScoped
public class TokenSessionValidator {
    private static final Logger LOG = Logger.getLogger(TokenSessionValidator.class);

    private final JsonWebToken jwt;
    private final TokenRepository tokenRepository;

    public TokenSessionValidator(
        JsonWebToken jwt, 
        TokenRepository tokenRepository
    ) {
        this.jwt = jwt;
        this.tokenRepository = tokenRepository;
    }

    @ServerRequestFilter(priority = 10)
    public void validateSession(ContainerRequestContext ctx) {
        LOG.debug("Validating token session...");

        // If no JWT present, skip validation
        if (jwt == null || jwt.getRawToken() == null) {
            LOG.debug("No JWT token found in the request.");
            return;
        }

        String rawToken = jwt.getRawToken();

        Token token = tokenRepository.findByAccessToken(rawToken)
            .orElseThrow(() -> new UnauthorizedException("Token not found"));

        if (Boolean.TRUE.equals(token.getRevoked())) {
            LOG.info("Attempt to use revoked token.");
            throw new UnauthorizedException("Token revoked");
        }

        // Inactivity expiration check (30 minutes)
        if (token.getLastUsedAt() != null &&
            token.getLastUsedAt().plusMinutes(30).isBefore(LocalDateTime.now())) {

            LOG.info("Session expired due to inactivity.");
            token.setRevoked(true);
            tokenRepository.persist(token);
            throw new UnauthorizedException("Session expired by inactivity");
        }

        // Update last used timestamp
        LOG.debug("Token session is valid. Updating last used timestamp.");
        token.setLastUsedAt(LocalDateTime.now());
        tokenRepository.persist(token);
    }

}

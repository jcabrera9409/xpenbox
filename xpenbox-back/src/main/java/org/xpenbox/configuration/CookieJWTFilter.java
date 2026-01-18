package org.xpenbox.configuration;

import org.jboss.logging.Logger;
import org.xpenbox.authorization.entity.Token;
import org.xpenbox.authorization.repository.TokenRepository;
import org.xpenbox.exception.UnauthorizedException;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.ext.Provider;

/**
 * CookieJWTFilter is a JAX-RS filter that intercepts incoming HTTP requests
 * to validate JWT tokens stored in cookies. It checks for the presence of
 * an "access_token" cookie, verifies the token against the database,
 * and ensures that the token has not been revoked. If the token is invalid
 * or revoked, an UnauthorizedException is thrown.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class CookieJWTFilter implements ContainerRequestFilter {
    private static final Logger LOG = Logger.getLogger(CookieJWTFilter.class);

    private final TokenRepository tokenRepository;
    private final SecurityIdentity securityIdentity;

    public CookieJWTFilter(
        TokenRepository tokenRepository, 
        SecurityIdentity securityIdentity) {
        this.tokenRepository = tokenRepository;
        this.securityIdentity = securityIdentity;
    }

    /**
     * Filters incoming requests to validate JWT tokens in cookies.
     * @param requestContext The context of the incoming request
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        LOG.debug("CookieJWTFilter invoked");

        // Skip validation for public endpoints
        String path = requestContext.getUriInfo().getPath();
        if (
            path.endsWith("/auth/login") || 
            path.endsWith("/auth/register") || 
            path.endsWith("/auth/refresh") || 
            path.endsWith("/auth/logout") ||
            path.startsWith("health")
        ) {
            LOG.debug("Public endpoint detected, skipping token validation: " + path);
            return;
        }

        if (securityIdentity.isAnonymous()) {
            LOG.debug("Request is anonymous, skipping token validation");
            return;
        }

        Cookie cookie = requestContext.getCookies().get("access_token");

        if (cookie == null) {
            LOG.debug("No access_token cookie found");
            return;
        }

        String token = cookie.getValue();
        LOG.debug("Access token from cookie: " + token);

        Token tokenEntity = tokenRepository.findByAccessToken(token)
            .orElseThrow(() -> { throw new UnauthorizedException("Invalid access token"); });
        
        LOG.debug("Token entity found: " + tokenEntity);

        if (tokenEntity.getRevoked()) {
            LOG.debug("Token has been revoked");
            throw new UnauthorizedException("Token has been revoked");
        }
        
    }
}

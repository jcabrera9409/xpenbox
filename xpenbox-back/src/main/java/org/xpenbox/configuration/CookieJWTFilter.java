package org.xpenbox.configuration;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.xpenbox.authorization.entity.Token;
import org.xpenbox.authorization.repository.TokenRepository;
import org.xpenbox.exception.UnauthorizedException;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTParser;
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
    private final JWTParser jwtParser;

    public CookieJWTFilter(
        TokenRepository tokenRepository, 
        SecurityIdentity securityIdentity,
        JWTParser jwtParser
    ) {
        this.tokenRepository = tokenRepository;
        this.securityIdentity = securityIdentity;
        this.jwtParser = jwtParser;
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

        String token = extractAccessTokenFromHeader(requestContext);

        if (token == null) {
            LOG.debug("No access token found in Authorization header, checking cookies...");
            token = extractAccessTokenFromCookie(requestContext);
            if (token == null) {
                LOG.debug("No access token found in cookies either");
                throw new UnauthorizedException("Access token is missing");
            }
        }

        LOG.debug("Access token from cookie or header: " + token);

        Token tokenEntity = tokenRepository.findByAccessToken(token)
            .orElseThrow(() -> { throw new UnauthorizedException("Invalid access token"); });
        
        LOG.debug("Token entity found: " + tokenEntity);

        try {
            JsonWebToken jwt = jwtParser.parse(token);
            LOG.debug("JWT parsed successfully: " + jwt.getRawToken());
        } catch (Exception e) {
            LOG.debug("Failed to parse JWT: " + e.getMessage());
            throw new UnauthorizedException("Invalid access token");
        }

        if (tokenEntity.getRevoked()) {
            LOG.debug("Token has been revoked");
            throw new UnauthorizedException("Token has been revoked");
        }

        LOG.debug("Token is valid and not revoked");
        
    }

    private String extractAccessTokenFromCookie(ContainerRequestContext requestContext) {
        Cookie cookie = requestContext.getCookies().get("access_token");
        if (cookie == null) {
            return null;
        }
        return cookie.getValue();
    }

    private String extractAccessTokenFromHeader(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

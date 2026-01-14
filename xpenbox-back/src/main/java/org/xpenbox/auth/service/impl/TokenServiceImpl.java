package org.xpenbox.auth.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.xpenbox.auth.entity.Token;
import org.xpenbox.auth.repository.TokenRepository;
import org.xpenbox.auth.service.TokenService;
import org.xpenbox.user.entity.User;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Implementation of the TokenService interface.
 */
@ApplicationScoped
public class TokenServiceImpl implements TokenService {
    private static final Logger LOG = Logger.getLogger(TokenServiceImpl.class);

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    private String issuer;

    @ConfigProperty(name = "mp.jwt.expiration.time")
    private long expirationTime;

    @ConfigProperty(name = "mp.jwt.refresh.expiration.time")
    private long refreshExpirationTime;

    private final TokenRepository tokenRepository;

    public TokenServiceImpl(
        TokenRepository tokenRepository
    ) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    @Override
    public Token createToken(User user, Boolean rememberMe) {
        LOG.infof("Creating token for user: %s, rememberMe: %b", user.getUsername(), rememberMe);

        String accessToken = generateAccessToken(user);
        
        String refreshToken = UUID.randomUUID().toString();

        Token token = new Token();
        token.setUser(user);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setPersistentSession(rememberMe);
        token.setIssuedAt(LocalDateTime.now());
        token.setLastUsedAt(LocalDateTime.now());
        token.setAccessTokenExpiresAt(LocalDateTime.now().plusSeconds(expirationTime));

        if (rememberMe) {
            token.setRefreshTokenExpiresAt(LocalDateTime.now().plusSeconds(refreshExpirationTime));
        }

        tokenRepository.persist(token);

        LOG.infof("Token created successfully for user: %s", user.getUsername());

        return token;
    }

    @Transactional
    @Override
    public Token refreshToken(String refreshToken) {
        LOG.infof("Refreshing token with refresh token: %s", refreshToken);

        Token token = tokenRepository.findValidRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired refresh token"));
        String newAccessToken = generateAccessToken(token.getUser());
        String newRefreshToken = UUID.randomUUID().toString();
        token.setAccessToken(newAccessToken);
        token.setRefreshToken(newRefreshToken);
        token.setIssuedAt(LocalDateTime.now());
        token.setLastUsedAt(LocalDateTime.now());
        token.setAccessTokenExpiresAt(LocalDateTime.now().plusSeconds(expirationTime));

        tokenRepository.persist(token);

        LOG.infof("Token refreshed successfully for user: %s", token.getUser().getUsername());

        return token;
    }

    @Transactional
    @Override
    public void revokeToken(Token token) {
        LOG.infof("Revoking token for user: %s", token.getUser().getUsername());
        token.setRevoked(true);
        tokenRepository.persist(token);
        LOG.infof("Token revoked successfully for user: %s", token.getUser().getUsername());
    }

    private String generateAccessToken(User user) {
        String accessToken = Jwt.issuer(issuer)
                .subject(user.getUsername())
                .expiresAt(System.currentTimeMillis() / 1000 + expirationTime)
                .sign();
        
        return accessToken;
    }
    
}

package org.xpenbox.authorization.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.xpenbox.authorization.dto.TokenResponseDTO;
import org.xpenbox.authorization.entity.Token;
import org.xpenbox.authorization.mapper.TokenMapper;
import org.xpenbox.authorization.repository.TokenRepository;
import org.xpenbox.authorization.service.ITokenService;
import org.xpenbox.common.ResourceCode;
import org.xpenbox.exception.ResourceNotFoundException;
import org.xpenbox.user.entity.User;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementation of the TokenService interface.
 */
@ApplicationScoped
public class TokenServiceImpl implements ITokenService {
    private static final Logger LOG = Logger.getLogger(TokenServiceImpl.class);

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    private String issuer;

    @ConfigProperty(name = "mp.jwt.expiration.time")
    private Long expirationTime;

    @ConfigProperty(name = "mp.jwt.refresh.expiration.time")
    private Long refreshExpirationTime;

    private final TokenRepository tokenRepository;

    public TokenServiceImpl(
        TokenRepository tokenRepository
    ) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public TokenResponseDTO createToken(User user, Boolean rememberMe) {
        LOG.infof("Creating token for email: %s, rememberMe: %b", user.getEmail(), rememberMe);

        Token token = new Token();
        token.setResourceCode(ResourceCode.generateTokenResourceCode());
        token.setUser(user);
        token.setPersistentSession(rememberMe);
        
        token = completeTokenData(token);

        tokenRepository.persist(token);

        LOG.infof("Token created successfully for email: %s", user.getEmail());

        return TokenMapper.toAuthDTO(token);
    }

    @Override
    public TokenResponseDTO refreshToken(String refreshToken) {
        LOG.infof("Refreshing token with refresh token: %s", refreshToken);

        Token token = tokenRepository.findValidRefreshToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired refresh token"));
        
        token = completeTokenData(token);

        tokenRepository.persist(token);

        LOG.infof("Token refreshed successfully for email: %s", token.getUser().getEmail());

        return TokenMapper.toAuthDTO(token);
    }

    @Override
    public void revokeToken(String refreshToken) {
        LOG.infof("Revoking token with refresh token: %s", refreshToken);
        Token token = tokenRepository.findValidRefreshToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired refresh token"));
        token.setRevoked(true);
        tokenRepository.persist(token);
        LOG.infof("Token revoked successfully for email: %s", token.getUser().getEmail());
    }

    private Token completeTokenData(Token token) {
        LOG.debugf("Completing token data for token ID: %d", token.getResourceCode());
        String accessToken = generateAccessToken(token.getUser());
        String refreshToken = UUID.randomUUID().toString();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setIssuedAt(LocalDateTime.now());
        token.setLastUsedAt(LocalDateTime.now());
        token.setAccessTokenExpiresAt(LocalDateTime.now().plusSeconds(expirationTime));

        if (token.getPersistentSession()) {
            token.setRefreshTokenExpiresAt(LocalDateTime.now().plusSeconds(refreshExpirationTime));
        }

        return token;
    }

    private String generateAccessToken(User user) {
        LOG.debugf("Generating access token for email: %s", user.getEmail());
        String accessToken = Jwt.issuer(issuer)
                .subject(user.getEmail())
                .expiresAt(System.currentTimeMillis() / 1000 + expirationTime)
                .sign();
        
        return accessToken;
    }
    
}

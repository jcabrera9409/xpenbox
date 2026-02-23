package org.xpenbox.authorization.service.impl;

import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.xpenbox.authorization.dto.TokenResponseDTO;
import org.xpenbox.authorization.entity.Token;
import org.xpenbox.authorization.mapper.TokenMapper;
import org.xpenbox.authorization.repository.TokenRepository;
import org.xpenbox.authorization.service.ITokenService;
import org.xpenbox.common.DateFunctions;
import org.xpenbox.common.ResourceCode;
import org.xpenbox.exception.ResourceNotFoundException;
import org.xpenbox.user.entity.User;

import io.quarkus.security.UnauthorizedException;
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

    @ConfigProperty(name = "mp.jwt.refresh.min.time")
    private Long refreshExpirationMinTime;

    @ConfigProperty(name = "mp.jwt.refresh.max.time")
    private Long refreshExpirationMaxTime;

    private final TokenRepository tokenRepository;
    private final TokenMapper tokenMapper;

    public TokenServiceImpl(
        TokenRepository tokenRepository,
        TokenMapper tokenMapper
    ) {
        this.tokenRepository = tokenRepository;
        this.tokenMapper = tokenMapper;
    }

    @Override
    public TokenResponseDTO createToken(User user, Boolean rememberMe) {
        LOG.infof("Creating token for email: %s, rememberMe: %b", user.getEmail(), rememberMe);

        Token token = new Token();
        token.setResourceCode(ResourceCode.generateTokenResourceCode());
        token.setUser(user);
        token.setPersistentSession(rememberMe);
        token.setRefreshTokenExpiresAt(DateFunctions.currentLocalDateTime().plusSeconds(
            rememberMe ? refreshExpirationMaxTime : refreshExpirationMinTime
        ));
        
        token = completeTokenData(token);

        tokenRepository.persist(token);

        LOG.infof("Token created successfully for email: %s", user.getEmail());

        return tokenMapper.toAuthDTO(token);
    }

    @Override
    public TokenResponseDTO refreshToken(String refreshToken) {
        LOG.infof("Refreshing token with refresh token: %s", refreshToken);

        Token token = tokenRepository.findValidRefreshToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired refresh token"));
        
        if (DateFunctions.currentLocalDateTime().isAfter(token.getRefreshTokenExpiresAt())) {
            LOG.warnf("Refresh token expired for email: %s", token.getUser().getEmail());
            throw new UnauthorizedException("Refresh token has expired");
        }

        token = completeTokenData(token);

        tokenRepository.persist(token);

        LOG.infof("Token refreshed successfully for email: %s", token.getUser().getEmail());

        return tokenMapper.toAuthDTO(token);
    }

    @Override
    public void revokeToken(String refreshToken) {
        LOG.infof("Revoking token with refresh token: %s", refreshToken);
        Token token = tokenRepository.findValidRefreshToken(refreshToken)
                .orElse(null);
        if (token == null) {
            LOG.warnf("No valid token found for revocation with refresh token: %s", refreshToken);
            return;
        }
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
        token.setIssuedAt(DateFunctions.currentLocalDateTime());
        token.setLastUsedAt(DateFunctions.currentLocalDateTime());
        token.setAccessTokenExpiresAt(DateFunctions.currentLocalDateTime().plusSeconds(expirationTime));
        
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

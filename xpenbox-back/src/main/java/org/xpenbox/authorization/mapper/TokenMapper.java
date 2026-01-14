package org.xpenbox.authorization.mapper;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.xpenbox.authorization.dto.TokenResponseDTO;
import org.xpenbox.authorization.entity.Token;

/**
 * Mapper class for converting Token entities to TokenResponseDTOs.
 */
public class TokenMapper {
    private static final Logger LOG = Logger.getLogger(TokenMapper.class);

    @ConfigProperty(name = "mp.jwt.expiration.time")
    private static Long expirationTime;

    @ConfigProperty(name = "mp.jwt.refresh.expiration.time")
    private static Long refreshExpirationTime;

    /**
     * Maps a Token entity to a TokenResponseDTO.
     * @param token the Token entity to be mapped
     * @return the corresponding TokenResponseDTO
     */
    public static TokenResponseDTO toAuthDTO(Token token) {
        LOG.infof("Mapping Token entity to TokenResponseDTO for token resource code: %d", token.getResourceCode());
        
        TokenResponseDTO dto = new TokenResponseDTO(
            token.getResourceCode(),
            token.getAccessToken(),
            token.getRefreshToken(),
            token.getAccessTokenExpiresAt(),
            token.getRefreshTokenExpiresAt(),
            token.getPersistentSession(),
            token.getRevoked(),
            token.getIssuedAt(),
            token.getLastUsedAt(),
            expirationTime,
            refreshExpirationTime
        );
        
        LOG.infof("Mapping completed for token resource code: %d", token.getResourceCode());
        return dto;
    }
}

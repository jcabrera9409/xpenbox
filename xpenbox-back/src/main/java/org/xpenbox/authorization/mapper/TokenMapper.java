package org.xpenbox.authorization.mapper;

import org.jboss.logging.Logger;
import org.xpenbox.authorization.dto.TokenResponseDTO;
import org.xpenbox.authorization.entity.Token;
import org.xpenbox.common.DateConvertir;

import jakarta.inject.Singleton;

/**
 * Mapper class for converting Token entities to TokenResponseDTOs.
 */
@Singleton
public class TokenMapper {
    private static final Logger LOG = Logger.getLogger(TokenMapper.class);

    /**
     * Maps a Token entity to a TokenResponseDTO.
     * @param token the Token entity to be mapped
     * @return the corresponding TokenResponseDTO
     */
    public TokenResponseDTO toAuthDTO(Token token) {
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
            // calculate expiresIn based on current time and accessTokenExpiresAt
            token.getAccessTokenExpiresAt() != null
                ? DateConvertir.calculateDuration(DateConvertir.currentLocalDateTime(), token.getAccessTokenExpiresAt()).getSeconds()
                : null,
            token.getRefreshTokenExpiresAt() != null
                ? DateConvertir.calculateDuration(DateConvertir.currentLocalDateTime(), token.getRefreshTokenExpiresAt()).getSeconds()
                : null
        );
        
        LOG.infof("Mapping completed for token resource code: %d", token.getResourceCode());
        return dto;
    }
}

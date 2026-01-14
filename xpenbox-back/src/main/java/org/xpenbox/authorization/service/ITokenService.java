package org.xpenbox.authorization.service;

import org.xpenbox.authorization.dto.TokenResponseDTO;
import org.xpenbox.user.entity.User;

/**
 * Service interface for managing Tokens.
 */
public interface ITokenService {
    
    /**
     * Creates a new Token for the specified user.
     *
     * @param user the user for whom the token is to be created
     * @param rememberMe flag indicating if the session should be persistent
     * @return the created TokenResponseDTO
     */
    TokenResponseDTO createToken(User user, Boolean rememberMe);

    /**
     * Refreshes the given Token with a new refresh token value.
     *
     * @param refreshToken the new refresh token value
     * @return the updated TokenResponseDTO
     */
    TokenResponseDTO refreshToken(String refreshToken);

    /**
     * Revokes the specified Token.
     *
     * @param refreshToken the refresh token to be revoked
     */
    void revokeToken(String refreshToken);
}

package org.xpenbox.auth.service;

import org.xpenbox.auth.entity.Token;
import org.xpenbox.user.entity.User;

/**
 * Service interface for managing Tokens.
 */
public interface TokenService {
    
    /**
     * Creates a new Token for the specified user.
     *
     * @param user the user for whom the token is to be created
     * @param rememberMe flag indicating if the session should be persistent
     * @return the created Token
     */
    public Token createToken(User user, Boolean rememberMe);

    /**
     * Refreshes the given Token with a new refresh token value.
     *
     * @param refreshToken the new refresh token value
     * @return the updated Token
     */
    public Token refreshToken(String refreshToken);

    /**
     * Revokes the specified Token.
     *
     * @param token the Token to be revoked
     */
    public void revokeToken(Token token);
}

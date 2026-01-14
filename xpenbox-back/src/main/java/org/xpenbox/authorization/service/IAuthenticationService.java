package org.xpenbox.authorization.service;

import org.xpenbox.authorization.dto.TokenResponseDTO;

/**
 * Service interface for authentication operations.
 */
public interface IAuthenticationService {
    
    /**
     * Authenticates a user and returns a token response.
     * @param email The email of the user.
     * @param password The password of the user.
     * @return A TokenResponseDTO containing access and refresh tokens.
     */
    TokenResponseDTO login(String email, String password, Boolean rememberMe);
}

package org.xpenbox.auth.service.impl;

import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;
import org.xpenbox.auth.dto.TokenResponseDTO;
import org.xpenbox.auth.service.IAuthenticationService;
import org.xpenbox.auth.service.ITokenService;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementation of the authentication service.
 */
@ApplicationScoped
public class AuthenticationServiceImpl implements IAuthenticationService {
    private static final Logger LOG = Logger.getLogger(AuthenticationServiceImpl.class);
    
    private final UserRepository userRepository;
    private final ITokenService tokenService;

    public AuthenticationServiceImpl(
        UserRepository userRepository,
        ITokenService tokenService
    ) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    public TokenResponseDTO login(String email, String password, Boolean rememberMe) {
        LOG.infof("Attempting login for email: %s", email);
        User user = userRepository.findByEmail(email).orElseThrow(
            () -> new IllegalArgumentException("Email not found")
        );

        if (!user.getVerified()) {
            LOG.infof("Login attempt for unverified email: %s", email);
            throw new IllegalStateException("Email not verified");
        }

        if (!user.getState()) {
            LOG.infof("Login attempt for inactive account: %s", email);
            throw new IllegalStateException("User account is inactive");
        }

        if(!BCrypt.checkpw(password, user.getPassword())) {
            LOG.infof("Password verification failed for email: %s", email);
            throw new IllegalArgumentException("Invalid password");
        }

        LOG.infof("Login successful for email: %s", email);
        return tokenService.createToken(user, rememberMe);
    }
    
}

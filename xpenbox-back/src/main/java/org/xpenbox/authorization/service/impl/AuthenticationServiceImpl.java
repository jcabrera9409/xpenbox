package org.xpenbox.authorization.service.impl;

import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;
import org.xpenbox.authorization.dto.TokenResponseDTO;
import org.xpenbox.authorization.service.IAuthenticationService;
import org.xpenbox.authorization.service.ITokenService;
import org.xpenbox.exception.EmailNotVerifiedException;
import org.xpenbox.exception.ForbiddenException;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

import io.quarkus.security.UnauthorizedException;
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
            () -> { throw new UnauthorizedException("Email not found"); }
        );

        if(!BCrypt.checkpw(password, user.getPassword())) {
            LOG.infof("Password verification failed for email: %s", email);
            throw new UnauthorizedException("Invalid password");
        }

        if (!user.getVerified()) {
            LOG.infof("Login attempt for unverified email: %s", email);
            throw new EmailNotVerifiedException("Email not verified");
        }

        if (!user.getState()) {
            LOG.infof("Login attempt for inactive account: %s", email);
            throw new ForbiddenException("User account is inactive");
        }

        LOG.infof("Login successful for email: %s", email);
        return tokenService.createToken(user, rememberMe);
    }
    
}

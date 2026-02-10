package org.xpenbox.user.service.impl;

import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;
import org.xpenbox.exception.ConflictException;
import org.xpenbox.exception.ResourceNotFoundException;
import org.xpenbox.user.dto.UserCreateDTO;
import org.xpenbox.user.dto.UserResponseDTO;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.mapper.UserMapper;
import org.xpenbox.user.repository.UserRepository;
import org.xpenbox.user.service.IUserService;
import org.xpenbox.user.service.IUserTokenService;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementation of IUserService for user-related operations.
 */
@ApplicationScoped
public class UserServiceImpl implements IUserService {
    private static final Logger LOG = Logger.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final IUserTokenService userTokenService;

    public UserServiceImpl(
        UserRepository userRepository,
        IUserTokenService userTokenService
    ) {
        this.userRepository = userRepository;
        this.userTokenService = userTokenService;
    }

    @Override
    public UserResponseDTO register(UserCreateDTO userRequest) {
        LOG.infof("Registering user with email: %s", userRequest.email());

        User userExists = userRepository.findByEmail(userRequest.email()).orElse(null);
        if (userExists != null) {
            LOG.warnf("User with email %s already exists", userRequest.email());
            throw new ConflictException("User with this email already exists");
        }
        
        User newUser = UserMapper.toEntity(userRequest);
        newUser.setPassword(BCrypt.hashpw(userRequest.password(), BCrypt.gensalt()));

        userRepository.persist(newUser);
        
        userTokenService.generateEmailVerificationToken(newUser.getEmail());
        
        LOG.infof("User with email %s registered successfully", userRequest.email());

        return UserMapper.toDTO(newUser);
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        LOG.infof("Retrieving user with email: %s", email);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            LOG.warnf("User with email %s not found", email);
            throw new ResourceNotFoundException("User with this email does not exist");
        }

        LOG.infof("User with email %s retrieved successfully", email);
        return UserMapper.toDTO(user);
    }
    
}

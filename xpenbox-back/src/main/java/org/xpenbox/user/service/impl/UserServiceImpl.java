package org.xpenbox.user.service.impl;

import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;
import org.xpenbox.user.dto.UserCreateDTO;
import org.xpenbox.user.dto.UserResponseDTO;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.mapper.UserMapper;
import org.xpenbox.user.repository.UserRepository;
import org.xpenbox.user.service.IUserService;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementation of IUserService for user-related operations.
 */
@ApplicationScoped
public class UserServiceImpl implements IUserService {
    private static final Logger LOG = Logger.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(
        UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponseDTO register(UserCreateDTO userRequest) {
        LOG.infof("Registering user with email: %s", userRequest.email());

        User userExists = userRepository.findByEmail(userRequest.email()).orElse(null);
        if (userExists != null) {
            LOG.warnf("User with email %s already exists", userRequest.email());
            throw new IllegalArgumentException("User with this email already exists");
        }
        
        User newUser = UserMapper.toEntity(userRequest);
        newUser.setPassword(BCrypt.hashpw(userRequest.password(), BCrypt.gensalt()));

        userRepository.persist(newUser);
        
        LOG.infof("User with email %s registered successfully", userRequest.email());

        return UserMapper.toDTO(newUser);
    }
    
}

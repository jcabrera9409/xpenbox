package org.xpenbox.user.service;

import org.xpenbox.user.dto.UserCreateDTO;
import org.xpenbox.user.dto.UserResponseDTO;

/**
 * Service interface for user-related operations.
 */
public interface IUserService {
    
    /**
     * Registers a new user.
     *
     * @param userRequest the user creation request data
     * @return the created user's response data
     */
    UserResponseDTO register(UserCreateDTO userRequest);

    /**
     * Retrieves user information by email.
     *
     * @param email the email of the user to retrieve
     * @return the user's response data
     */
    UserResponseDTO getUserByEmail(String email);
}

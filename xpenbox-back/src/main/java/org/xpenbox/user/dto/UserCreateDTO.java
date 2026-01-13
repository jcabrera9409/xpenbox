package org.xpenbox.user.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating a new user.
 * @param username the username of the user
 * @param email the email of the user
 * @param password the password of the user
 * @param currency the preferred currency of the user
 */
@RegisterForReflection
public record UserCreateDTO (

    @NotNull(message = "Username cannot be null")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,

    @NotNull(message = "Email cannot be null")
    @Email(message = "Email should be valid")
    @Size(max = 250, message = "Email must be at most 250 characters")
    String email,

    @NotNull(message = "Password cannot be null")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,

    @NotNull(message = "Currency cannot be null")
    @Size(min = 1, max = 10, message = "Currency must be between 1 and 10 characters")
    String currency
){ }

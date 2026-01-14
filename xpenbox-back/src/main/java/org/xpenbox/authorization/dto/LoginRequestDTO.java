package org.xpenbox.authorization.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for Login requests.
 * @param email The email of the user.
 * @param password The password of the user.
 * @param rememberMe Flag indicating if the session should be persistent.
 */
@RegisterForReflection
public record LoginRequestDTO (

    @NotNull(message = "Email cannot be null")
    @Email(message = "Email should be valid")
    @Size(max = 250, message = "Email must be at most 250 characters")
    String email,

    @NotNull(message = "Password cannot be null")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,

    @NotNull(message = "RememberMe cannot be null")
    Boolean rememberMe
) { }

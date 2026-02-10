package org.xpenbox.authorization.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for email verification requests.
 * @param email The email address to verify.
 */
@RegisterForReflection
public record VerifyEmailRequestDTO (
    @NotNull(message = "Email cannot be null")
    @Email(message = "Email should be valid")
    String email
) { }

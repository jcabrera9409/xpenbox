package org.xpenbox.user.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** Data Transfer Object for updating an existing user. 
 * @param email the new email of the user
 * @param password the new password of the user
 * @param currency the new preferred currency of the user
*/
@RegisterForReflection
public record UserUpdateDTO (

    @Email(message = "Email should be valid")
    @Size(max = 250, message = "Email must be at most 250 characters")
    String email,

    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,

    @Size(min = 1, max = 10, message = "Currency must be between 1 and 10 characters")
    String currency
) { }

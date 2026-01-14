package org.xpenbox.user.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/** Data Transfer Object for user response.
 * @param username the username of the user
 * @param email the email of the user
 * @param currency the preferred currency of the user
 * @param verified the verification status of the user
*/
@RegisterForReflection
public record UserResponseDTO (
    String email,
    String currency,
    Boolean verified
) { }

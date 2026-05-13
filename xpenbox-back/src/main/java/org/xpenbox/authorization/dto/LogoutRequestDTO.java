package org.xpenbox.authorization.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for logout requests, containing the FCM token to be removed from the user's session.
 * @param fcmToken The FCM token associated with the user's session, which should be removed upon logout. Must be at most 500 characters long.
 */
@RegisterForReflection
public record LogoutRequestDTO (
    @Size(max = 500, message = "FCM token must be at most 500 characters long")
    String fcmToken
) { }

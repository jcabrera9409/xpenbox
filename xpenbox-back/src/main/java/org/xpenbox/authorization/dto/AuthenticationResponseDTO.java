package org.xpenbox.authorization.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * AuthenticationResponseDTO is a data transfer object that encapsulates the response for authentication-related operations.
 * It contains the access token and refresh token issued to the user upon successful authentication.
 * @param accessToken The JWT access token for the authenticated user
 * @param refreshToken The JWT refresh token for obtaining new access tokens without re-authenticating
 */
@RegisterForReflection
public record AuthenticationResponseDTO(
    String accessToken,
    String refreshToken
) { }

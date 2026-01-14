package org.xpenbox.authorization.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Token responses.
 * @param resourceCode the resource code associated with the token
 * @param accessToken the access token string
 * @param refreshToken the refresh token string
 * @param accessTokenExpiresAt the expiration date and time of the access token
 * @param refreshTokenExpiresAt the expiration date and time of the refresh token
 * @param persistentSession flag indicating if the session is persistent
 * @param revoked flag indicating if the token has been revoked
 * @param issuedAt the date and time when the token was issued
 * @param lastUsedAt the date and time when the token was last used
 * @param accessTokenExpiresIn the expiration time of the access token in seconds
 * @param refreshTokenExpiresIn the expiration time of the refresh token in seconds
 */
public record TokenResponseDTO (
    String resourceCode,
    String accessToken,
    String refreshToken,
    LocalDateTime accessTokenExpiresAt,
    LocalDateTime refreshTokenExpiresAt,
    Boolean persistentSession,
    Boolean revoked,
    LocalDateTime issuedAt,
    LocalDateTime lastUsedAt,
    Long accessTokenExpiresIn,
    Long refreshTokenExpiresIn
) { }

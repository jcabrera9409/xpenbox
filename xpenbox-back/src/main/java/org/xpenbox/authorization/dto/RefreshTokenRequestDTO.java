package org.xpenbox.authorization.dto;

/**
 * RefreshTokenRequestDTO is a data transfer object used for requesting a new access token using a refresh token.
 * @param refreshToken The refresh token used to obtain a new access token.
 */
public record RefreshTokenRequestDTO(
    String refreshToken
) { }


/**
 * Data Transfer Object for the response of the authentication process, containing the access token and refresh token.
 * Contains the access token and refresh token returned after a successful authentication.
 */
export interface AuthenticationResponseDTO {
    accessToken: string;
    refreshToken: string;
}
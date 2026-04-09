/**
 * Data Transfer Object representing the user response.
 * Includes user's email, preferred currency, and verification status.
 */
export interface UserResponseDTO {
    email: string;
    currency: string;
    verified: boolean;
}
import { signal } from "@angular/core";
import { UserResponseDTO } from "../model/user.response.dto";

/**
 * State management for user-related data.
 * Includes loading status, error messages, and logged-in user information.
 */
export const userState = {
    isLoading: signal<boolean>(false),
    error: signal<string | null>(null),
    userLogged: signal<UserResponseDTO | null>(null),
}
import { signal } from '@angular/core';

/**
 * State management for authentication-related data.
 * Includes signals for loading status, authentication status,
 * error messages, and session verification status.
 * This centralized state can be used across the authentication feature
 * to maintain consistency and reactivity.
 */
export const authState = {
  isLoading: signal<boolean>(false),
  isAuthenticated: signal<boolean>(false),
  error: signal<string | null>(null),
  sessionVerified: signal<boolean>(false), 
};

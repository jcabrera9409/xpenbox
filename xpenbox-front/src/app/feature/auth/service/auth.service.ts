import { Injectable } from '@angular/core';
import { EnvService } from '../../common/service/env.service';
import { HttpClient } from '@angular/common/http';
import { LoginRequestDTO } from '../model/login.request.dto';
import { authState } from './auth.state';
import { Observable, tap, catchError, throwError, map } from 'rxjs';
import { ApiResponseDTO } from '../../common/model/api.response.dto';
import { UserRequestDTO } from '../../user/model/user.request.dto';

/**
 * Service for handling authentication-related operations
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private envService: EnvService
  ) {
    this.apiUrl = `${this.envService.getApiUrl()}/auth`;
  }

  /**
   * Register a new user with the provided user data
   * @param userRequest User registration data transfer object
   * @returns Observable that completes when registration is successful
   */
  register(userRequest: UserRequestDTO): Observable<ApiResponseDTO<void>> {
    return this.http.post<ApiResponseDTO<void>>(`${this.apiUrl}/register`, userRequest);
  }

  /**
   * Verify email using the provided token
   * @param token Email verification token sent to the user's email
   * @returns Observable that completes when email verification is successful
   */
  verifyEmail(token: string): Observable<ApiResponseDTO<void>> {
    return this.http.get<ApiResponseDTO<void>>(`${this.apiUrl}/verify-email?token=${token}`);
  }

  /**
   * Resend email verification token
   * @param email User's email address
   * @returns Observable that completes when the email verification token is resent
   */
  verifyEmailResend(email: string): Observable<ApiResponseDTO<void>> {
    return this.http.post<ApiResponseDTO<void>>(`${this.apiUrl}/verify-email/resend`, 
      { 'email': email }
    );
  }

  /**
   * Login user with credentials
   * @param credentials User login data transfer object
   * @returns Observable that completes when login is successful
   */
  login(credentials: LoginRequestDTO): Observable<void> {
    authState.isLoading.set(true);
    return this.http.post<void>(`${this.apiUrl}/login`, credentials, { 
      withCredentials: true 
    }).pipe(
      tap(() => {
        authState.isAuthenticated.set(true);
        authState.error.set(null);
        authState.isLoading.set(false);
      })
    );
  }

  /**
   * Refresh the authentication token
   * @returns Observable that completes when the token is refreshed
   */
  refresh(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/refresh`, {}, { 
      withCredentials: true,
      observe: 'response' 
    }).pipe(
      tap(() => {
        authState.isAuthenticated.set(true);
        authState.error.set(null);
      }),
      map(() => void 0), 
      catchError((error) => {
        authState.isAuthenticated.set(false);
        authState.error.set('Sesión expirada');
        return throwError(() => error);
      })
    );
  }

  /**
   * Logout the current user
   * @returns Observable that completes when logout is successful
   */
  logout(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/logout`, {}, { 
      withCredentials: true 
    }).pipe(
      tap(() => {
        authState.isAuthenticated.set(false);
      })
    );
  }

  /**
   * Check if the current session is valid
   * @returns Observable that completes when the session is verified
   */
  checkSession(): Observable<void> {
    return this.http.get<void>(`${this.apiUrl}/check`, { 
      withCredentials: true 
    }).pipe(
      tap(() => {
        authState.isAuthenticated.set(true);
        authState.error.set(null);
      }),
      catchError((error) => {
        authState.isAuthenticated.set(false);
        authState.error.set('Sesión expirada');
        return throwError(() => error);
      })
    );
  }

  /**
   * Clear authentication state
   * Resets isAuthenticated, error, and sessionVerified states
   */
  clearAuthState(): void {
    authState.isAuthenticated.set(false);
    authState.error.set(null);
    authState.sessionVerified.set(false);
  }
}

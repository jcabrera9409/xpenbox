import { HttpErrorResponse, HttpHandlerFn, HttpRequest } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "../../auth/service/auth.service";
import { Router } from "@angular/router";
import { catchError, Observable, switchMap, throwError } from "rxjs";
import { NotificationService } from "../service/notification.service";

// Singleton to track ongoing refresh token requests
let ongoingRefresh$: Observable<void> | null = null;

/**
 * Authentication Interceptor to handle token refresh on 401 errors
 * @param req The outgoing HTTP request
 * @param next The next interceptor in the chain
 * @returns An Observable of the HTTP event stream
 */
export function authInterceptor(
    req: HttpRequest<any>,
    next: HttpHandlerFn
) {
    const notificationService = inject(NotificationService);
    const authService = inject(AuthService);
    const router = inject(Router);

    return next(req.clone({withCredentials: true})).pipe(
        catchError((error: HttpErrorResponse) => {
            if (req.url.endsWith('/auth/refresh')) {
                authService.clearAuthState();
                ongoingRefresh$ = null;
                return throwError(() => error);
            }

            if (req.url.endsWith('/auth/logout')) {
                return throwError(() => error);
            }

            if (error.status !== 401) {
                return throwError(() => error); 
            }

            const currentUrl = router.url;
            if (currentUrl.includes('/login') || currentUrl.includes('/register')) {
                return throwError(() => error);
            }

            // If a refresh is already in progress, reuse it
            if (!ongoingRefresh$) {
                ongoingRefresh$ = authService.refresh().pipe(
                    catchError((refreshError) => {
                        ongoingRefresh$ = null;
                        authService.logout().subscribe({
                            complete: () => {
                                notificationService.error('Tu sesión ha expirado. Por favor, inicia sesión nuevamente.');
                                authService.clearAuthState();
                                router.navigate(['/login']);
                            },
                            error: () => {
                                authService.clearAuthState();
                                router.navigate(['/login']);
                            }
                        });
                        return throwError(() => refreshError);
                    })
                );
            }

            return ongoingRefresh$.pipe(
                switchMap(() => {
                    ongoingRefresh$ = null;
                    return next(req.clone({withCredentials: true}));
                })
            );
        })
    )
}
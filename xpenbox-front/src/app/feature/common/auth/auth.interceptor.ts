import { HttpErrorResponse, HttpHandlerFn, HttpRequest } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "../../auth/service/auth.service";
import { Router } from "@angular/router";
import { catchError, switchMap, throwError } from "rxjs";

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
    const authService = inject(AuthService);
    const router = inject(Router);

    return next(req.clone({withCredentials: true})).pipe(
        catchError((error: HttpErrorResponse) => {
            if (req.url.endsWith('/auth/refresh')) {
                authService.clearAuthState();
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

            return authService.refresh().pipe(
                switchMap(() => 
                    next(req.clone({withCredentials: true}))
                ),
                catchError((refreshError) => {
                    authService.logout().subscribe({
                        complete: () => {
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
        })
    )
}
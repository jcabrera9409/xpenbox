import { inject, PLATFORM_ID } from "@angular/core";
import { isPlatformBrowser } from "@angular/common";
import { CanActivateFn, Router } from "@angular/router";
import { authState } from "../../auth/service/auth.state";
import { AuthService } from "../../auth/service/auth.service";
import { map, catchError, of, take } from "rxjs";

/**
 * Authentication guard to protect routes that require user authentication.
 * @returns A boolean or an Observable that resolves to a boolean indicating whether the route can be activated.
 */
export const authGuard: CanActivateFn = () => {
    const router = inject(Router);
    const authService = inject(AuthService);
    const platformId = inject(PLATFORM_ID);

    if (!isPlatformBrowser(platformId)) {
        return true;
    }

    authState.sessionVerified.set(false);
    
    return authService.checkSession().pipe(
        take(1),
        map(() => {
            authState.sessionVerified.set(true);
            return true;
        }),
        catchError(() => {
            authService.logout().subscribe({
                complete: () => {
                    authService.clearAuthState();
                }
            });
            authState.sessionVerified.set(true);
            router.navigate(['/login']);
            return of(false);
        })
    );
}

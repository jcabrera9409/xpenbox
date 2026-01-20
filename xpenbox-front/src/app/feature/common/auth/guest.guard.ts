import { CanActivateFn, Router } from "@angular/router";
import { authState } from "../../auth/service/auth.state";
import { inject, PLATFORM_ID } from "@angular/core";
import { isPlatformBrowser } from "@angular/common";
import { AuthService } from "../../auth/service/auth.service";
import { map, catchError, of, take } from "rxjs";

/**
 * Guest Guard to prevent authenticated users from accessing guest-only routes
 * @returns  A boolean or an Observable that resolves to a boolean indicating if the route can be activated
 */
export const guestGuard: CanActivateFn = () => {
    const router = inject(Router);
    const authService = inject(AuthService);
    const platformId = inject(PLATFORM_ID);

    if (!isPlatformBrowser(platformId)) {
        return true;
    }

    if (authState.isAuthenticated() && authState.sessionVerified()) {
        router.navigate(['/landing']);
        return false;
    }

    if (!authState.sessionVerified()) {
        return authService.checkSession().pipe(
            take(1),
            map(() => {
                authState.sessionVerified.set(true);
                router.navigate(['/landing']);
                return false;
            }),
            catchError(() => {
                authState.sessionVerified.set(true);
                return of(true);
            })
        );
    }

    return true;
}
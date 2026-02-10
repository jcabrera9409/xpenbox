import { Routes } from '@angular/router';
import { LoginPage } from './pages/auth/login/login.page';
import { RegisterPage } from './pages/auth/register/register.page';
import { landingRoutes } from './pages/landing.routes';
import { LandingPage } from './pages/landing/landing.page';
import { guestGuard } from './feature/common/auth/guest.guard';
import { authGuard } from './feature/common/auth/auth.guard';
import { VerifyEmailPage } from './pages/auth/verify-email/verify-email.page';

export const routes: Routes = [
    { path: '', redirectTo: '/login', pathMatch: 'full' },
    { path: 'verify-email', component: VerifyEmailPage },
    { path: 'login', component: LoginPage, canActivate: [guestGuard] },
    { path: 'register', component: RegisterPage, canActivate: [guestGuard] },
    { path: 'landing', component: LandingPage, children: landingRoutes, canActivate: [authGuard] }
];

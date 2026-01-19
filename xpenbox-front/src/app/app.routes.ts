import { Routes } from '@angular/router';
import { LoginPage } from './pages/auth/login/login.page';
import { RegisterPage } from './pages/auth/register/register.page';
import { landingRoutes } from './pages/landing.routes';
import { LandingPage } from './pages/landing/landing.page';

export const routes: Routes = [
    { path: 'login', component: LoginPage },
    { path: 'register', component: RegisterPage },
    { path: 'landing', component: LandingPage, children: landingRoutes },
];

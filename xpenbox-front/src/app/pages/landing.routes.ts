import { Routes } from '@angular/router';
import { DashboardPage } from './dashboard/dashboard.page';
import { AccountPage } from './account/account.page';

export const landingRoutes: Routes = [
    { path: '', component: DashboardPage },
    { path: 'account', component: AccountPage }
];

import { Routes } from '@angular/router';
import { DashboardPage } from './dashboard/dashboard.page';
import { AccountPage } from './account/account.page';
import { CategoryPage } from './category/category.page';
import { SettingsPage } from './settings/settings.page';

export const landingRoutes: Routes = [
    { path: '', component: DashboardPage },
    { path: 'account', component: AccountPage },
    { path: 'category', component: CategoryPage },
    { path: 'settings', component: SettingsPage },
];

import { signal } from "@angular/core";

export const DashboardState = {
    isLoadingDashboardData: signal<boolean>(false),
    errorDashboardData: signal<string | null>(null),
}
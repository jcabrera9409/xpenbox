import { Injectable } from '@angular/core';
import { CapacitorService } from './capacitor.service';
import { AuthService } from '../../auth/service/auth.service';
import { App, AppState } from '@capacitor/app';
import { authState } from '../../auth/service/auth.state';
import { Router } from "@angular/router";
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AppStateService {
  private isInitialized = false;
  private lastBackgroundTime: number | null = null;
  private readonly SESSION_TIMEOUT = 15 * 60 * 1000; 

  constructor(
    private capacitorService: CapacitorService,
    private authService: AuthService,
    private router: Router
  ) {}

  async initialize(): Promise<void> {
    if (this.isInitialized || !this.capacitorService.isNativePlatform()) {
      return;
    }

    App.addListener('appStateChange', async (state: AppState) => {
      console.log('App state changed:', state.isActive);

      if (state.isActive) {
        // App has come to the foreground
        await this.handleAppResume();
      } else {
        // App has gone to the background
        this.handleAppPause();
      }
    });

    this.isInitialized = true;
  }

  private handleAppPause(): void {
    // Record the time when the app goes to the background
    this.lastBackgroundTime = Date.now();
    console.log('App paused at:', this.lastBackgroundTime);
  }

  private async handleAppResume(): Promise<void> {
    console.log('App resumed');

    if (!authState.isAuthenticated()) {
      console.log('User is not authenticated, no need to check session timeout');
      return;
    }

    if (this.lastBackgroundTime) {
      const timeInBackground = Date.now() - this.lastBackgroundTime;
      console.log('Time in background (ms):', timeInBackground);

      if (timeInBackground > this.SESSION_TIMEOUT) {
        console.log('Session has timed out, logging out user');
        await this.refreshSessionSafely();
      } else {
        console.log('Session is still valid, no action needed');
      }
    }

    this.lastBackgroundTime = null;
  }

  private async refreshSessionSafely(): Promise<void> {
    try {
      const refreshToken = await this.capacitorService.getRefreshToken();

      if (!refreshToken) {
        console.log('No refresh token found, logging out user');
        this.authService.clearAuthState();

        const currentUrl = this.router.url;
        if (!currentUrl.includes('/login') && !currentUrl.includes('/register')) {
          this.router.navigate(['/login']);
        }
        return;
      }

      await firstValueFrom(this.authService.refresh());
      console.log('Session refreshed successfully');
    } catch (error) {
      console.error('Error refreshing session:', error);
      this.authService.clearAuthState();
    }
  }

  async cleanup(): Promise<void> {
    if (this.isInitialized) {
      await App.removeAllListeners();
      this.isInitialized = false;
    }
  }
}

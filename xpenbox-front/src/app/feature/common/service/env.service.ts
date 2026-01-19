import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class EnvService {

  private production: boolean = false;
  private apiUrl: string = '';
  private domains: string[] = [];
  private googleAnalyticsId: string = '';
  private platformId = inject(PLATFORM_ID);

  constructor() { 
    let env: any = {};
    
    if (isPlatformBrowser(this.platformId)) {
      env = (window as any).__env || {};
      this.production = env.production || false;
      this.apiUrl = env.apiUrl || 'http://localhost:8080';
      this.domains = env.domains || ['localhost:8080'];
      this.googleAnalyticsId = env.googleAnalyticsId || '';
    } 
  }

  isProduction(): boolean {
    return this.production;
  }

  getApiUrl(): string {
    return this.apiUrl;
  }

  getDomains(): string[] {
    return this.domains;
  }

  getGoogleAnalyticsId(): string {
    return this.googleAnalyticsId;
  }
}
import { Component, effect, inject, PLATFORM_ID, signal } from '@angular/core';
import { CommonModule, isPlatformServer } from '@angular/common';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../feature/auth/service/auth.service';

@Component({
  selector: 'app-verify-email-page',
  imports: [CommonModule, LoadingUi, RouterLink],
  templateUrl: './verify-email.page.html',
  styleUrl: './verify-email.page.css',
})
export class VerifyEmailPage {

  isLoading = signal<boolean>(true);
  errorVerifyEmail = signal<string | null>(null);

  token = signal<string | undefined>(undefined);

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
  ) {
    const platformId = inject(PLATFORM_ID);
    if (isPlatformServer(platformId)) {
      return;
    }

    this.route.queryParamMap.subscribe(params => {
      this.token.set(params.get('token') || undefined);
    });

    effect(() => {
      if (!this.token()) {
        this.isLoading.set(false);
        this.errorVerifyEmail.set('No se proporcionó un token de verificación.');
        return;
      }

      this.verifyEmail();
    });
  }

  verifyEmail() {
    this.isLoading.set(true);
    this.errorVerifyEmail.set(null);

    this.authService.verifyEmail(this.token()!).subscribe({
      next: () => {
        this.isLoading.set(false);
      },
      error: (error) => {
        this.isLoading.set(false);
        if(error.status === 500 || error.status === 0) {
          this.errorVerifyEmail.set('Error del servidor. Por favor, inténtalo de nuevo más tarde.');
        } else {
          this.errorVerifyEmail.set(error.error?.message || 'Error desconocido. Por favor, inténtalo de nuevo.');
        }
      }
    });
  }
}

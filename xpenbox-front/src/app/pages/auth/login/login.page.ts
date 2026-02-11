import { Component, ChangeDetectionStrategy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../feature/auth/service/auth.service';
import { LoginRequestDTO } from '../../../feature/auth/model/login.request.dto';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { StorageService } from '../../../shared/service/storage.service';

@Component({
  selector: 'app-login-page',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LoadingUi],
  templateUrl: './login.page.html',
  styleUrl: './login.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
})
export class LoginPage {
  protected readonly loginForm: FormGroup;
  protected readonly showPassword = signal(false);
  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly errorStatus = signal<number | null>(null);

  protected readonly sendVerificationEmail = signal(false);
  protected readonly errorVerificationEmail = signal<string | null>(null);
  protected readonly showResendVerification = signal(false);

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly storageService: StorageService,
    private readonly router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      rememberMe: [false]
    });
  }

  protected verifyEmail(): void {
    const email = this.loginForm.value.email;
    if (!email) return;

    this.showResendVerification.set(false);
    this.sendVerificationEmail.set(true);
    this.errorVerificationEmail.set(null);

    this.authService.verifyEmailResend(email).subscribe({
      next: () => {
        this.showResendVerification.set(true);
        this.sendVerificationEmail.set(false);
      }, 
      error: (error) => {
        this.sendVerificationEmail.set(false);
        if (error.status === 500 || error.status === 0) {
          this.errorVerificationEmail.set('Ocurrió un error al enviar el correo de verificación. Por favor, inténtalo de nuevo.');
        } else {
          this.errorVerificationEmail.set(error.error?.message || 'Ocurrió un error al enviar el correo de verificación. Por favor, inténtalo de nuevo.');
        }
      }
    });
  }

  protected togglePasswordVisibility(): void {
    this.showPassword.update(value => !value);
  }

  protected onSubmit(): void {
    if (this.loginForm.valid) {
      this.isSubmitting.set(true);

      const credentials: LoginRequestDTO = new LoginRequestDTO(
        this.loginForm.value.email,
        this.loginForm.value.password,
        this.loginForm.value.rememberMe
      );
      
      this.authService.login(credentials).subscribe({
        next: () => {
          this.isSubmitting.set(false);
          this.storageService.setHasLoggedBefore(true);
          this.router.navigate(['/landing']);
        },
        error: (error) => {
          if (error.status === 401) {
            this.errorMessage.set('Correo electrónico o contraseña incorrectos.');
          } else if (error.status === 403) {
            this.errorMessage.set('Tu correo no está verificado o está deshabilitado.');
          } else if (error.status === 428) {
            this.errorMessage.set('Tu correo no está verificado.');
          } else {
            this.errorMessage.set('Ocurrió un error al iniciar sesión. Por favor, inténtalo de nuevo más tarde.');
          }
          this.isSubmitting.set(false);
          this.errorStatus.set(error.status);
        }
      });

    } else {
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
    }
  }
}

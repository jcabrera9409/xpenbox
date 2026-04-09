import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../feature/auth/service/auth.service';

@Component({
  selector: 'app-forgot-password-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.page.html',
  styleUrl: './forgot-password.page.css',
})
export class ForgotPasswordPage {
  forgotPasswordForm: FormGroup;
  
  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);
  emailSent = signal(false);
  resetToken = signal<string | undefined>(undefined);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.forgotPasswordForm.invalid) {
      this.forgotPasswordForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const email = this.forgotPasswordForm.get('email')?.value;

    this.authService.resetPasswordRequest(email).subscribe({
      next: () => {
        this.emailSent.set(true);
        this.isSubmitting.set(false);
      },
      error: (error) => {
        if (error.status === 500 || error.status === 0) {
          this.errorMessage.set('Error de conexión. Por favor, inténtalo de nuevo más tarde.');
        }
        this.isSubmitting.set(false);
      }
    });
  }
}

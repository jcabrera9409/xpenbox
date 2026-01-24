import { Component, ChangeDetectionStrategy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../feature/auth/service/auth.service';
import { LoginRequestDTO } from '../../../feature/auth/model/login.request.dto';

@Component({
  selector: 'app-login-page',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.page.html',
  styleUrl: './login.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
})
export class LoginPage {
  protected readonly loginForm: FormGroup;
  protected readonly showPassword = signal(false);
  protected readonly isSubmitting = signal(false);

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      rememberMe: [false]
    });
  }

  protected togglePasswordVisibility(): void {
    this.showPassword.update(value => !value);
  }

  protected onSubmit(): void {
    if (this.loginForm.valid) {
      const credentials: LoginRequestDTO = new LoginRequestDTO(
        this.loginForm.value.email,
        this.loginForm.value.password,
        this.loginForm.value.rememberMe
      );
      
      this.authService.login(credentials).subscribe({
        next: () => {
          this.isSubmitting.set(false);
          this.router.navigate(['/landing']);
        },
        error: (error) => {
          this.isSubmitting.set(false);
          console.error('Error al iniciar sesión:', error);
        }
      });

      setTimeout(() => {
        this.isSubmitting.set(false);
      }, 1000);

    } else {
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
    }
  }
}

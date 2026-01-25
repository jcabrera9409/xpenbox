import { Component, ChangeDetectionStrategy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../feature/auth/service/auth.service';
import { UserRequestDTO } from '../../../feature/user/model/user.request.dto';
import { NotificationService } from '../../../feature/common/service/notification.service';

@Component({
  selector: 'app-register-page',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.page.html',
  styleUrl: './register.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterPage {
  registerForm: FormGroup;
  showPassword = signal(false);
  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  currencies = [
    { value: 'PEN', label: 'PEN - Sol Peruano' },
    { value: 'USD', label: 'USD - Dólar Americano' }
  ];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router,
  ) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      defaultCurrency: ['PEN', Validators.required]
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword.update(value => !value);
  }

  onSubmit(): void {
    if (this.registerForm.invalid) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    
    const formData = this.registerForm.value;
    const email = formData.email;
    const password = formData.password;
    const defaultCurrency = formData.defaultCurrency;

    const registerData = new UserRequestDTO(email, password, defaultCurrency);

    this.authService.register(registerData).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.notificationService.success('Cuenta creada exitosamente. Por favor, inicia sesión.');
        this.router.navigate(['/login']);
      }, error: (error) => {
        this.isSubmitting.set(false);
        if (error.status === 409) {
          this.errorMessage.set('El correo electrónico ya está en uso. Por favor, utiliza otro.');
        } else {
          this.errorMessage.set('Ocurrió un error al crear la cuenta. Por favor, inténtalo de nuevo más tarde.');
        }
      }
    });
  }

  getErrorMessage(fieldName: string): string {
    const control = this.registerForm.get(fieldName);
    
    if (!control || !control.touched || !control.errors) {
      return '';
    }

    if (control.errors['required']) {
      return 'Este campo es requerido';
    }

    if (fieldName === 'email' && control.errors['email']) {
      return 'El email no es válido';
    }

    if (fieldName === 'password' && control.errors['minlength']) {
      return 'La contraseña debe tener al menos 8 caracteres';
    }

    return '';
  }

  hasError(fieldName: string): boolean {
    const control = this.registerForm.get(fieldName);
    return !!(control && control.touched && control.invalid);
  }
}

import { Component, ChangeDetectionStrategy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

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

  currencies = [
    { value: 'PEN', label: 'PEN - Sol Peruano' },
    { value: 'USD', label: 'USD - Dólar Americano' }
  ];

  constructor(private fb: FormBuilder) {
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
    if (this.registerForm.valid && !this.isSubmitting()) {
      this.isSubmitting.set(true);
      
      const formData = this.registerForm.value;
      console.log('Registro:', formData);
      
      // TODO: Implementar llamada al servicio de autenticación
      // Simulación de registro
      setTimeout(() => {
        this.isSubmitting.set(false);
      }, 2000);
    } else {
      // Marcar todos los campos como tocados para mostrar errores
      Object.keys(this.registerForm.controls).forEach(key => {
        this.registerForm.get(key)?.markAsTouched();
      });
    }
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

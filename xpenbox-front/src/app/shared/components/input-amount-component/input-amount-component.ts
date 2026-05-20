import { Component, input, output, Output } from '@angular/core';
import { FormControl } from '@angular/forms';
import { defaultInputErrorDTO, InputErrorDTO } from '../../dto/input-error.dto';
import { userState } from '../../../feature/user/service/user.state';
import { CommonModule } from '@angular/common';
import { IconComponent } from '../icon.component/icon.component';
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-input-amount-component',
  imports: [CommonModule, IconComponent, ReactiveFormsModule],
  templateUrl: './input-amount-component.html',
  styleUrl: './input-amount-component.css',
})
export class InputAmountComponent {
  inputControl = input<FormControl>(new FormControl());
  inputName = input<string>();
  
  userState = userState;

  amountValue = output<number>();

  get currency(): string {
    return this.userState.userLogged()?.currency ?? '';
  }

  get requiredMessageError(): string | null {
    const inputError: InputErrorDTO | undefined = defaultInputErrorDTO.get(this.inputName() ?? '');
    if (inputError && inputError.requiredError) return inputError.requiredError;
    return `El Monto es obligatorio.`;
  }

  get minValueMessageError(): string | null {
    const inputError: InputErrorDTO | undefined = defaultInputErrorDTO.get(this.inputName() ?? '');
    if (inputError && inputError.minValueError) return inputError.minValueError;
    return `El Monto debe ser al menos ${this.minValueValidator}.`;
  }

  get maxValueMessageError(): string | null {
    const inputError: InputErrorDTO | undefined = defaultInputErrorDTO.get(this.inputName() ?? '');
    if (inputError && inputError.maxValueError) return inputError.maxValueError;
    return `El Monto debe ser como máximo ${this.maxValueValidator}.`;
  }

  get minValueValidator(): number | null {
    const errors = this.inputControl()?.errors;
    return errors?.['min']?.min ?? null;
  }

  get maxValueValidator(): number | null {
    const errors = this.inputControl()?.errors;
    return errors?.['max']?.max ?? null;
  }

  get formattedAmount(): string {
    // Obtiene el valor actual del input, lo interpreta como centavos y lo muestra como PEN 0.00
    const value = this.inputControl()?.value;
    if (value === null || value === undefined || value === '') return '0.00';
    let str = value.toString().replace(/\D/g, '');
    if (!str) return '0.00';
    while (str.length < 3) str = '0' + str;
    const intValue = parseInt(str, 10);
    const formatted = (intValue / 100).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    return formatted;
  }

  // Maneja el input para formatear el valor mientras se escribe
  onAmountInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    // Elimina el prefijo de moneda y espacios (solo al inicio)
    let raw = input.value.replace(new RegExp('^' + this.currency + '\\s*'), '').replace(/[^\d]/g, '');
    if (!raw) raw = '0';
    // Limita a 9 dígitos para evitar overflow
    if (raw.length > 9) raw = raw.slice(0, 9);
    // Actualiza el FormControl con el valor limpio
    this.inputControl().setValue(raw, { emitEvent: false });
    // Actualiza el valor mostrado en el input
    input.value = this.currency + ' ' + this.formattedAmount;
    // Mueve el cursor al final

    const amountValue = parseInt(raw, 10) / 100;
    this.amountValue.emit(amountValue);

    setTimeout(() => {
      input.setSelectionRange(input.value.length, input.value.length);
    });
  }
}

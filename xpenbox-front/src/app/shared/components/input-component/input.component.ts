import { Component, input } from '@angular/core';
import { FormControl } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { IconComponent } from '../icon.component/icon.component';
import { ReactiveFormsModule } from '@angular/forms';
import { defaultInputErrorDTO, InputErrorDTO } from '../../dto/input-error.dto';

@Component({
  selector: 'app-input-component',
  imports: [CommonModule, IconComponent, ReactiveFormsModule],
  templateUrl: './input.component.html',
  styleUrl: './input.component.css',
})
export class InputComponent {
  inputControl = input<FormControl>(new FormControl());
  inputName = input<string>();

  inputType = input<string>('text');
  placeholder = input<string>('');

  labelText = input<string | null>(null);
  iconName = input<string | null>(null);
  
  get requiredMessageError(): string | null {
    const inputError: InputErrorDTO | undefined = defaultInputErrorDTO.get(this.inputName() ?? '');
    if (inputError && inputError.requiredError) return inputError.requiredError;
    return `El ${this.labelText()} es obligatorio.`;
  }

  get minLengthMessageError(): string | null {
    const inputError: InputErrorDTO | undefined = defaultInputErrorDTO.get(this.inputName() ?? '');
    if (inputError && inputError.minLengthError) return inputError.minLengthError;
    return `Debe tener mínimo ${this.minLengthValidator} caracteres.`;
  }

  get maxLengthMessageError(): string | null {
    const inputError: InputErrorDTO | undefined = defaultInputErrorDTO.get(this.inputName() ?? '');
    if (inputError && inputError.maxLengthError) return inputError.maxLengthError;
    return `Debe tener máximo ${this.maxLengthValidator} caracteres.`;
  }

  get minValueMessageError(): string | null {
    const inputError: InputErrorDTO | undefined = defaultInputErrorDTO.get(this.inputName() ?? '');
    if (inputError && inputError.minValueError) return inputError.minValueError;
    return `El ${this.labelText()} debe ser al menos ${this.minValueValidator}.`;
  }

  get maxValueMessageError(): string | null {
    const inputError: InputErrorDTO | undefined = defaultInputErrorDTO.get(this.inputName() ?? '');
    if (inputError && inputError.maxValueError) return inputError.maxValueError;
    return `El ${this.labelText()} debe ser como máximo ${this.maxValueValidator}.`;
  }

  get minLengthValidator(): number | null {
    const errors = this.inputControl()?.errors;
    return errors?.['minlength']?.requiredLength ?? null;
  }

  get maxLengthValidator(): number | null {
    const errors = this.inputControl()?.errors;
    return errors?.['maxlength']?.requiredLength ?? null;
  }

  get minValueValidator(): number | null {
    const errors = this.inputControl()?.errors;
    return errors?.['min']?.min ?? null;
  }

  get maxValueValidator(): number | null {
    const errors = this.inputControl()?.errors;
    return errors?.['max']?.max ?? null;
  }
}

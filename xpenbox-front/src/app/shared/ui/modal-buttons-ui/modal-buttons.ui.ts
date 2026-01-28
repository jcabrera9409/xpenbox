import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-modal-buttons-ui',
  imports: [],
  templateUrl: './modal-buttons.ui.html',
  styleUrl: './modal-buttons.ui.css',
})
export class ModalButtonsUi {

  cancelText = input<string>('Cancelar');
  cancelIcon = input<string>('close');

  isEditMode = input<boolean>(false)
  isLoading = input<boolean>(false);
  isValidForm = input<boolean>(false);
  loadingText = input<string>('Guardando...');
  loadingIcon = input<string>('autorenew');
  
  confirmText = input<string>('Registrar');
  confirmIcon = input<string>('add');

  confirmEditText = input<string>('Actualizar');
  confirmEditIcon = input<string>('check');

  cancel = output<void>();
  confirm = output<void>();

  onCancel() {
    this.cancel.emit();
  }

  onConfirm() {
    this.confirm.emit();
  }
}

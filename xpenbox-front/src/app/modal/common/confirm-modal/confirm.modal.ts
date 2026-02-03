import { Component, input, OnInit, output, signal } from '@angular/core';
import { ModalButtonsUi } from '../../../shared/ui/modal-buttons-ui/modal-buttons.ui';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { RetryComponent } from '../../../shared/components/retry-component/retry.component';

@Component({
  selector: 'app-confirm-modal',
  imports: [ModalButtonsUi, LoadingUi, RetryComponent],
  templateUrl: './confirm.modal.html',
  styleUrl: './confirm.modal.css',
})
export class ConfirmModal {
  // Inputs
  title = input<string>('Confirmar acción');
  message = input<string>('¿Estás seguro de que deseas continuar?');
  confirmText = input<string>('Confirmar');
  confirmIcon = input<string>('warning');
  loadingText = input<string>('Procesando...');
  resourceCode = input<string | null>(null);

  isLoadingConfirm = input<boolean>(false);
  isErrorSending = input<string | null>(null);
  isLoading = input<boolean>(false);
  isErrorLoading = input<string | null>(null);

  // Outputs
  confirmed = output<string>();
  cancelled = output<void>();

  onConfirm(): void {
    this.confirmed.emit(this.resourceCode()!);
  }

  onCancel(): void {
    this.cancelled.emit();
  }
}

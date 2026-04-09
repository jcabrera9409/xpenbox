import { Component } from '@angular/core';
import { genericState } from '../../../feature/common/service/generic.state';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-receipt-modal',
  imports: [CommonModule],
  templateUrl: './receipt.modal.html',
  styleUrl: './receipt.modal.css',
})
export class ReceiptModal {
  genericState = genericState;

  get showModal() {
    return this.genericState.showReceiptModal();
  }

  get title() {
    return this.genericState.titleReceiptModal();
  }

  get content() {
    return this.genericState.contentReceiptModal();
  }

  get buttonText() {
    return this.genericState.buttonTextReceiptModal();
  }

  get buttonIcon() {
    return this.genericState.buttonIconReceiptModal();
  }

  closeModal() {
    this.genericState.showReceiptModal.set(false);
    this.genericState.titleReceiptModal.set('');
    this.genericState.contentReceiptModal.set('');
    this.genericState.buttonTextReceiptModal.set('');
    this.genericState.buttonIconReceiptModal.set('');
  }
}

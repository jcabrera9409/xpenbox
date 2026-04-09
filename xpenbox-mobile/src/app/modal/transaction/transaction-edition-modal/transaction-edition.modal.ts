import { Component, input, OnInit, output, signal } from '@angular/core';
import { TransactionResponseDTO } from '../../../feature/transaction/model/transaction.response.dto';
import { transactionState } from '../../../feature/transaction/service/transaction.state';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { RetryComponent } from '../../../shared/components/retry-component/retry.component';
import { ModalButtonsUi } from '../../../shared/ui/modal-buttons-ui/modal-buttons.ui';
import { NotificationService } from '../../../feature/common/service/notification.service';
import { TransactionService } from '../../../feature/transaction/service/transaction.service';
import { DateService } from '../../../shared/service/date.service';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { CategoryResponseDTO } from '../../../feature/category/model/category.response.dto';
import { CategoriesCarouselComponent } from '../../../shared/components/categories-carousel-component/categories-carousel.component';
import { TransactionRequestDTO } from '../../../feature/transaction/model/transaction.request.dto';
import { userState } from '../../../feature/user/service/user.state';

@Component({
  selector: 'app-transaction-edition-modal',
  imports: [CommonModule, ReactiveFormsModule, LoadingUi, RetryComponent, ModalButtonsUi, CategoriesCarouselComponent],
  templateUrl: './transaction-edition.modal.html',
  styleUrl: './transaction-edition.modal.css',
})
export class TransactionEditionModal implements OnInit {
  
  userLogged = userState.userLogged;

  resourceCodeSelected = input<string | null>();
  close = output<void>();

  transactionData = signal<TransactionResponseDTO | null>(null);
  transactionState = transactionState;

  selectedCategory = signal<CategoryResponseDTO | null>(null);
  assignToCategory = signal<boolean>(false);

  formTransaction!: FormGroup;

  maxDate = signal('');

  constructor(
    private fb: FormBuilder,
    private transactionService: TransactionService,
    private notificationService: NotificationService,
    private dateService: DateService
  ) { }
  
  ngOnInit(): void {
    this.transactionState.isLoadingSendingTransaction.set(false);
    this.transactionState.errorSendingTransaction.set(null);

    this.loadTransactionData();
    this.initForm();
  }

  get descriptionControl() {
    return this.formTransaction.get('description');
  }

  get transactionDateControl() {
    return this.formTransaction.get('transactionDate');
  }

  onSubmit() {
    if (this.formTransaction.invalid) return;

    const transactionRequest = new TransactionRequestDTO();
    const transactionDate = this.dateService.parseDatetimeIsoString(this.transactionDateControl?.value);
    transactionRequest.description = this.descriptionControl?.value || '';
    transactionRequest.transactionDateTimestamp = this.dateService.toTimestamp(transactionDate);

    if (this.assignToCategory() && this.selectedCategory()) {
      transactionRequest.categoryResourceCode = this.selectedCategory()?.resourceCode;
    }

    this.transactionState.isLoadingSendingTransaction.set(true);

    this.transactionService.update(this.resourceCodeSelected()!, transactionRequest).subscribe({
      next: (response: ApiResponseDTO<TransactionResponseDTO>) => {
        this.transactionState.isLoadingSendingTransaction.set(false);
        if (response.success && response.data) {
          this.close.emit();
          this.notificationService.success('Transacción actualizada con éxito.');
          this.transactionState.transactionCreatedResourceCode.set(response.data.resourceCode);
        } else {
          this.transactionState.errorSendingTransaction.set(response.message);
        }
      }, error: (error) => {
        this.transactionState.isLoadingSendingTransaction.set(false);
        if (error.status === 500 || error.status === 0) {
          this.transactionState.errorSendingTransaction.set('Error actualizando la transacción. Por favor, inténtalo de nuevo.');
        } else {
          this.transactionState.errorSendingTransaction.set(error.error.message || 'Error actualizando la transacción.');
        }
      }
    });
  }

  onClose() {
    console.log(this.formTransaction.value);
    this.close.emit();
  }

  selectCategory(category: CategoryResponseDTO): void {
    this.selectedCategory.set(category);
  }

  getErrorMessage(controlName: string): string {
    const control = this.formTransaction.get(controlName);
    if (!control || !control.errors || !control.touched) {
      return '';
    }

    if (control.errors['required']) {
      return 'Este campo es obligatorio';
    }

    if (controlName === 'description') {
      if (control.errors['minlength']) {
        return 'La descripción debe tener al menos 3 caracteres';
      }
      if (control.errors['maxlength']) {
        return 'La descripción no puede exceder 100 caracteres';
      }
    }

    return '';
  }

  retryLoadTransactionData() {
    this.loadTransactionData();
  }

  private initForm() {
    const today = this.dateService.getLocalDatetime();
    this.maxDate.set(this.dateService.format(today.getTime(), 'ISO-LOCAL').split('T')[0]);

    this.formTransaction = this.fb.group({
      description: ['', [Validators.minLength(3), Validators.maxLength(100)]],
      transactionDate: ['', [Validators.required]],
    });
  }

  private loadTransactionData() {

    this.transactionState.isLoadingGetTransaction.set(true);

    this.transactionService.getByResourceCode(this.resourceCodeSelected()!).subscribe({
      next: (response: ApiResponseDTO<TransactionResponseDTO>) => {
        this.transactionState.isLoadingGetTransaction.set(false);
        if (response.success && response.data) {
          this.transactionData.set(response.data);
          const description = response.data.description || '';
          const transactionDate = this.dateService.toLocalDate(response.data.transactionDateTimestamp);
          const formattedDate = this.dateService.format(transactionDate.getTime(), 'ISO-LOCAL');
          const categoryResourceCode = response.data.category || null;

          this.formTransaction.patchValue({
            description: description,
            transactionDate: formattedDate
          });

          if (categoryResourceCode) {
            this.selectedCategory.set(response.data.category!);
            this.assignToCategory.set(true);
          }
        } else {
          this.transactionState.errorGetTransaction.set(response.message);
        }
      }, error: (error) => {
        if (error.status === 500 || error.status === 0) {
          this.transactionState.errorGetTransaction.set('Error cargando la transacción. Por favor, inténtalo de nuevo.');
        } else {
          this.transactionState.errorGetTransaction.set(error.error.message || 'Error cargando los datos de la transacción');
        }
        this.transactionState.isLoadingGetTransaction.set(false);
      }
    });
  }
}

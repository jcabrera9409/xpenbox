import { Component, OnInit, output, signal, input } from '@angular/core';
import { CreditCardResponseDTO } from '../../../feature/creditcard/model/creditcard.response.dto';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreditCardService } from '../../../feature/creditcard/service/creditcard.service';
import { CommonModule } from '@angular/common';
import { CreditCardRequestDTO } from '../../../feature/creditcard/model/creditcard.request.dto';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { creditCardState } from '../../../feature/creditcard/service/creditcard.state';
import { NotificationService } from '../../../feature/common/service/notification.service';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';

@Component({
  selector: 'app-creditcard-edition-modal',
  imports: [CommonModule, ReactiveFormsModule, LoadingUi],
  templateUrl: './creditcard-edition.modal.html',
  styleUrl: './creditcard-edition.modal.css',
})
export class CreditcardEditionModal implements OnInit {

  resourceCodeSelected = input<string | null>();
  close = output<void>();

  creditCardData = signal<CreditCardResponseDTO | null>(null);
  creditCardState = creditCardState;

  formCreditCard!: FormGroup;
  loading = signal<boolean>(false);
  sendingForm = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private creditCardService: CreditCardService,
    private notificationService: NotificationService
  ) { }

  ngOnInit(): void {
    this.loadCreditCardData()
    this.initForms();
  }

  get isEditMode(): boolean {
    return this.resourceCodeSelected() !== null;
  }

  onSubmit() {
    if (this.formCreditCard.invalid) return;

    this.sendingForm.set(true);
    this.errorMessage.set(null);

    const creditCardData = this.buildCreditCardData()

    const observable = this.isEditMode
      ? this.creditCardService.update(this.resourceCodeSelected()!, creditCardData)
      : this.creditCardService.create(creditCardData);

    observable.subscribe({
      next: (response: ApiResponseDTO<CreditCardResponseDTO>) => {
        this.sendingForm.set(false);

        if(response.success && response.data) {
          this.notificationService.success(`Tarjeta de crédito ${this.isEditMode ? 'actualizada' : 'creada'} con éxito.`);
          this.creditCardService.refresh();
          this.close.emit()
        } else {
          this.creditCardState.error.set(response.message);
        }
      }, error: () => {
        this.creditCardState.error.set('Error guardando la tarjeta de crédito. Por favor, inténtalo de nuevo más tarde.');
        this.sendingForm.set(false);
      }
    });
  }

  onClose() {
    this.close.emit();
  }

  private buildCreditCardData(): CreditCardRequestDTO {
    const formValues = this.formCreditCard.value;
    const name = formValues.name;
    const creditLimit = formValues.creditLimit;
    const currentBalance = this.isEditMode ? null : formValues.currentBalance;
    const billingDay = formValues.billingDay;
    const paymentDay = formValues.paymentDay;


    return new CreditCardRequestDTO(
      name,
      creditLimit,
      currentBalance,
      billingDay,
      paymentDay,
      true
    );
  }

  private initForms() {
    this.formCreditCard = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(150)]],
      creditLimit: [0, [Validators.required, Validators.min(1)]],
      currentBalance: [0, [Validators.required, Validators.min(0)]],
      billingDay: [1, [Validators.required, Validators.min(1), Validators.max(28)]],
      paymentDay: [1, [Validators.required, Validators.min(1), Validators.max(28)]]
    });

    const currentBalanceControl = this.formCreditCard.get('currentBalance');
    if (this.isEditMode) {
      currentBalanceControl?.clearValidators();
      currentBalanceControl?.updateValueAndValidity();
    }
  }

  private loadCreditCardData() {
    if (!this.isEditMode) return;

    this.loading.set(true);

    this.creditCardService.getByResourceCode(this.resourceCodeSelected()!).subscribe({
      next: (response: ApiResponseDTO<CreditCardResponseDTO>) => {
        this.loading.set(false);
        if (response.success && response.data) {
          this.creditCardData.set(response.data)
          this.formCreditCard.patchValue({
            name: response.data.name,
            creditLimit: response.data.creditLimit,
            billingDay: response.data.billingDay,
            paymentDay: response.data.paymentDay,
          });
        } else {
          console.error('Error fetching credit card data:', response.message);
          this.errorMessage.set('Error fetching credit card data: ' + response.message);
        }
      },
      error: (error) => {
        console.error('Error fetching credit card data:', error);
        this.errorMessage.set(error.message || 'Error fetching credit card data');
        this.loading.set(false);
      }
    })
  }
}

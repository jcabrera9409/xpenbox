import { Component, OnInit, output, signal, input } from '@angular/core';
import { CreditCardResponseDTO } from '../../../feature/creditcard/model/creditCardResponseDTO';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreditCardService } from '../../../feature/creditcard/service/creditcard.service';
import { CommonModule } from '@angular/common';
import { CreditCardRequestDTO } from '../../../feature/creditcard/model/creditCardRequestDTO';
import { ApiResponseDTO } from '../../../feature/common/model/apiResponseDTO';

@Component({
  selector: 'app-creditcard-edition-modal',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './creditcard-edition.modal.html',
  styleUrl: './creditcard-edition.modal.css',
})
export class CreditcardEditionModal implements OnInit {

  resourceCodeSelected = input<string | null>();
  close = output<void>();

  creditCardData = signal<CreditCardResponseDTO | null>(null);

  formCreditCard!: FormGroup;
  loading = signal<boolean>(false);
  sendingForm = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private creditCardService: CreditCardService
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
          this.creditCardService.refresh();
          this.close.emit()
        } else {
          console.error('Error creating credit card:', response.message);
          this.errorMessage.set('Error creating account: ' + response.message);
        }
      }, error: (error) => {
        console.error('Error creating credit card:', error);
        this.errorMessage.set(error.message || 'Error creating credit card');
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

import { Component, OnInit, output, ChangeDetectionStrategy, signal, input, effect } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { incomeState } from '../../../feature/income/service/income.state';
import { IncomeResponseDTO } from '../../../feature/income/model/income.response.dto';
import { IncomeService } from '../../../feature/income/service/income.service';
import { NotificationService } from '../../../feature/common/service/notification.service';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { IncomeRequestDTO } from '../../../feature/income/model/income.request.dto';
import { accountState } from '../../../feature/account/service/account.state';
import { AccountCreditDTO } from '../../../shared/dto/account-credit.dto';
import { AccountService } from '../../../feature/account/service/account.service';
import { AccountCreditService } from '../../../shared/service/account-credit.service';
import { AccountsCarouselComponent } from '../../../shared/components/accounts-carousel-component/accounts-carousel.component';
import { RetryComponent } from '../../../shared/components/retry-component/retry.component';
import { ModalButtonsUi } from '../../../shared/ui/modal-buttons-ui/modal-buttons.ui';
import { DateService } from '../../../shared/service/date.service';

@Component({
  selector: 'app-income-edition-modal',
  imports: [CommonModule, ReactiveFormsModule, LoadingUi, AccountsCarouselComponent, RetryComponent, ModalButtonsUi],
  templateUrl: './income-edition.modal.html',
  styleUrl: './income-edition.modal.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class IncomeEditionModal implements OnInit {

  resourceCodeSelected = input<string | null>();
  close = output<void>();

  accountState = accountState;
  incomeState = incomeState;
  incomeData = signal<IncomeResponseDTO | null>(null);

  selectedAccount = signal<AccountCreditDTO | null>(null);
  accountsList = signal<AccountCreditDTO[]>([]);
  assignToAccount = signal<boolean>(false);
  
  formIncome!: FormGroup;

  maxDate = signal('');

  constructor(
    private fb: FormBuilder,
    private incomeService: IncomeService,
    private accountService: AccountService,
    private accountCreditService: AccountCreditService,
    private notificationService: NotificationService,
    private dateService: DateService
  ) {
    if (this.accountState.accounts().length === 0) {
      this.accountService.load();
    }

    // Auto-select first account when loaded and sort accounts
    effect(() => {
      const accounts = this.accountCreditService.combineAccountAndCreditCardData(this.accountState.accounts(), []);
      const filteredAccounts = this.accountCreditService.filterAndSortAccountCredits(accounts, 0);
      if (filteredAccounts.length > 0) {
        this.accountsList.set(filteredAccounts);
        
        if (!this.selectedAccount()) {
          this.selectedAccount.set(filteredAccounts[0] || null);
        }
      }
    });
  }

  ngOnInit(): void {
    this.incomeState.isLoadingSendingIncome.set(false);
    this.incomeState.errorSendingIncome.set(null);

    this.loadIncomeData();
    this.initForm();
  }

  get isEditMode(): boolean {
    return this.resourceCodeSelected() !== null;
  }

  get isValidForm(): boolean {
    return this.formIncome.valid && (!this.assignToAccount() || (this.assignToAccount() && this.selectedAccount() !== null));
  }

  onClose(): void {
    this.close.emit();
  }

  onSubmit(): void {
    if (!this.isValidForm) return;

    this.incomeState.isLoadingSendingIncome.set(true);
    this.incomeState.errorSendingIncome.set(null);
    
    const incomeRequest = this.buildIncomeData();

    const observable = this.isEditMode
      ? this.incomeService.update(this.resourceCodeSelected()!, incomeRequest)
      : this.incomeService.create(incomeRequest);

    observable.subscribe({
      next: (response: ApiResponseDTO<IncomeResponseDTO>) => {
        this.incomeState.isLoadingSendingIncome.set(false);

        if (response.success && response.data) {
          this.notificationService.success(`Ingreso ${this.isEditMode ? 'actualizado' : 'creado'} con éxito.`);
          this.incomeService.refresh();
          this.close.emit();
        }
      }, error: (error) => {
        if (error.status === 500 || error.status === 0) {
          this.incomeState.errorSendingIncome.set('Error guardando el ingreso. Por favor, inténtalo de nuevo más tarde.');
        } else {
          this.incomeState.errorSendingIncome.set(error.error.message || 'Error guardando el ingreso');
        }
        this.incomeState.isLoadingSendingIncome.set(false);
      }
    });
  }

  // Getters para acceso fácil a controles
  get conceptControl() {
    return this.formIncome.get('concept');
  }

  get amountControl() {
    return this.formIncome.get('amount');
  }

  get incomeDateControl() {
    return this.formIncome.get('incomeDate');
  }

  // Método helper para mostrar errores
  getErrorMessage(controlName: string): string {
    const control = this.formIncome.get(controlName);
    if (!control || !control.errors || !control.touched) {
      return '';
    }

    if (control.errors['required']) {
      return 'Este campo es obligatorio';
    }

    if (controlName === 'concept') {
      if (control.errors['minlength']) {
        return 'El concepto debe tener al menos 3 caracteres';
      }
      if (control.errors['maxlength']) {
        return 'El concepto no puede exceder 150 caracteres';
      }
    }

    if (controlName === 'amount') {
      if (control.errors['min']) {
        return 'El monto debe ser mayor a 0';
      }
    }

    return '';
  }

  retryLoadIncomeData(): void {
    this.loadIncomeData();
  }

  retryLoadAccountsData(): void {
    this.accountService.refresh();
  }

  private buildIncomeData(): IncomeRequestDTO {
    const formValues = this.formIncome.value;
    const concept = formValues['concept'];
    const incomeDate = this.dateService.toUtcDate(new Date(formValues['incomeDate']));
    const incomeDateTimestamp = this.dateService.toTimestamp(incomeDate);
    const totalAmount = formValues['amount'];
    const accountResourceCode: string | undefined = !this.isEditMode && this.assignToAccount() && this.selectedAccount()
      ? this.selectedAccount()!.resourceCode
      : undefined;

    return new IncomeRequestDTO(concept, incomeDateTimestamp, totalAmount, accountResourceCode);
  }
  
  private initForm(): void {
    const today = this.dateService.getLocalDatetime();
    this.maxDate.set(this.dateService.format(today.getTime(), 'ISO').split('T')[0]);

    this.formIncome = this.fb.group({
      concept: ['', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(150)
      ]],
      amount: [null, [
        Validators.required,
        Validators.min(0.01)
      ]],
      incomeDate: [this.maxDate(), [Validators.required]]
    });
  }

  private loadIncomeData(): void {
    if (!this.isEditMode) return;

    this.incomeState.isLoadingGetIncome.set(true);

    this.incomeService.getByResourceCode(this.resourceCodeSelected()!).subscribe({
      next: (response: ApiResponseDTO<IncomeResponseDTO>) => {
        this.incomeState.isLoadingGetIncome.set(false);
        if (response.success && response.data) {
          this.incomeData.set(response.data);
          const incomeDate = this.dateService.toDate(response.data.incomeDateTimestamp);
          const formattedDate = this.dateService.format(incomeDate.getTime(), 'ISO').split('T')[0];
          this.formIncome.patchValue({
            concept: response.data.concept,
            amount: response.data.totalAmount,
            incomeDate: formattedDate
          });
        } else {
          this.incomeState.errorGetIncome.set(response.message);
        }
      }, error: (error) => {
        if (error.status === 500 || error.status === 0) {
          this.incomeState.errorGetIncome.set('Error cargando los datos del ingreso. Por favor, inténtalo de nuevo.');
        } else {
          this.incomeState.errorGetIncome.set(error.error.message || 'Error cargando los datos del ingreso');
        }
        this.incomeState.isLoadingGetIncome.set(false);
      }
    });
  }
}

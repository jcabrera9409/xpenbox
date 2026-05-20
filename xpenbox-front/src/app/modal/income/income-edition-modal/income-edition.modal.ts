import { Component, OnInit, output, ChangeDetectionStrategy, signal, input, effect } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { incomeState } from '../../../feature/income/service/income.state';
import { IncomeResponseDTO } from '../../../feature/income/model/income.response.dto';
import { IncomeService } from '../../../feature/income/service/income.service';
import { NotificationService } from '../../../feature/common/service/notification.service';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { IncomeRequestDTO } from '../../../feature/income/model/income.request.dto';
import { accountState } from '../../../feature/account/service/account.state';
import { AccountCreditDTO } from '../../../shared/dto/account-credit.dto';
import { AccountService } from '../../../feature/account/service/account.service';
import { AccountCreditService } from '../../../shared/service/account-credit.service';
import { AccountsCarouselComponent } from '../../../shared/components/accounts-carousel-component/accounts-carousel.component';
import { DateService } from '../../../shared/service/date.service';
import { transactionState } from '../../../feature/transaction/service/transaction.state';
import { userState } from '../../../feature/user/service/user.state';
import { upgradeProModalState } from '../../subscription/state/upgrade-pro.modal.state';
import { IconComponent } from '../../../shared/components/icon.component/icon.component';
import { InputComponent } from '../../../shared/components/input-component/input.component';
import { InputAmountComponent } from '../../../shared/components/input-amount-component/input-amount-component';
import { GenericModal } from '../../common/generic-modal/generic.modal';

@Component({
  selector: 'app-income-edition-modal',
  imports: [CommonModule, ReactiveFormsModule, AccountsCarouselComponent, IconComponent, InputComponent, InputAmountComponent, GenericModal],
  templateUrl: './income-edition.modal.html',
  styleUrl: './income-edition.modal.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class IncomeEditionModal implements OnInit {

  userLogged = userState.userLogged;

  close = output<void>();

  accountState = accountState;
  incomeState = incomeState;
  transactionState = transactionState;

  selectedAccount = signal<AccountCreditDTO | null>(null);
  accountsList = signal<AccountCreditDTO[]>([]);
  
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
      const filteredAccounts = this.accountCreditService.filterAndSortAccountCredits(accounts, -1);
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

    this.initForm();
  }

  get isValidForm(): boolean {
    return this.formIncome.valid && this.selectedAccount() !== null;
  }

  onClose(): void {
    this.close.emit();
  }

  onSubmit(): void {
    if (!this.isValidForm) return;

    this.incomeState.isLoadingSendingIncome.set(true);
    this.incomeState.errorSendingIncome.set(null);
    
    const incomeRequest = this.buildIncomeData();

    const observable = this.incomeService.create(incomeRequest);

    observable.subscribe({
      next: (response: ApiResponseDTO<IncomeResponseDTO>) => {
        this.incomeState.isLoadingSendingIncome.set(false);
        if (response.success && response.data) {
          this.incomeService.refresh();
          
          if (incomeRequest.accountResourceCode) {
            this.transactionState.transactionCreatedResourceCode.set(response.data.resourceCode);
            this.transactionState.successSendingTransaction.set(true);
            this.accountService.refresh();
          } else {
            this.notificationService.success('Ingreso creado correctamente');
          }
          
          this.close.emit();
        }
      }, error: (error) => {
        if (error.status === 500 || error.status === 0) {
          this.incomeState.errorSendingIncome.set('Error guardando el ingreso. Por favor, inténtalo de nuevo más tarde.');
        } else if (error.status === 403) {
           if (error.error && error.error.featureCode) {
            this.showUpgradeProModal();
            } else {
              transactionState.errorSendingTransaction.set('No tienes permiso para realizar esta acción. Por favor, contacta con soporte.');
            } 
        } else {
          this.incomeState.errorSendingIncome.set(error.error.message || 'Error guardando el ingreso');
        }
        this.incomeState.isLoadingSendingIncome.set(false);
      }
    });
  }

  private showUpgradeProModal(): void {
    upgradeProModalState.title.set('¡Estás usando Xpenbox a todo ritmo!');
    upgradeProModalState.htmlMessage.set('Ya registraste 50 transacciones en tu plan Free.' +
              ' Actualiza a <strong>Pro</strong> para seguir registrando todos tus gastos, ingresos y movimientos sin límites.');
    upgradeProModalState.open.set(true);
  }

  // Getters para acceso fácil a controles
  get conceptControl() {
    return this.formIncome.get('concept') as FormControl;
  }

  get amountControl() {
    return this.formIncome.get('amount') as FormControl;
  }

  get incomeDateControl() {
    return this.formIncome.get('incomeDate') as FormControl;
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

  retryLoadAccountsData(): void {
    this.accountService.refresh();
  }

  private buildIncomeData(): IncomeRequestDTO {
    const formValues = this.formIncome.value;
    const concept = formValues['concept'];

    const incomeDate = this.dateService.parseDatetimeIsoString(formValues['incomeDate']);
    const incomeDateTimestamp = this.dateService.toTimestamp(incomeDate);
    const totalAmount = formValues['amount'] / 100;
    const accountResourceCode: string | undefined = this.selectedAccount()
      ? this.selectedAccount()!.resourceCode
      : undefined;

    return new IncomeRequestDTO(concept, incomeDateTimestamp, totalAmount, accountResourceCode);
  }
  
  private initForm(): void {
    const today = this.dateService.toTimestamp(this.dateService.getLocalDatetime());
    const formattedDate = this.dateService.format(today, 'ISO-LOCAL');
    this.maxDate.set(formattedDate);

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
}

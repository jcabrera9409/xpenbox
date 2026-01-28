import { Component, ChangeDetectionStrategy, output, input, OnInit, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AccountService } from '../../../feature/account/service/account.service';
import { AccountRequestDTO } from '../../../feature/account/model/account.request.dto';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { AccountResponseDTO } from '../../../feature/account/model/account.response.dto';
import { accountState } from '../../../feature/account/service/account.state';
import { NotificationService } from '../../../feature/common/service/notification.service';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { RetryComponent } from '../../../shared/components/retry-component/retry.component';
import { ModalButtonsUi } from '../../../shared/ui/modal-buttons-ui/modal-buttons.ui';

@Component({
  selector: 'app-account-edition-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LoadingUi, RetryComponent, ModalButtonsUi],
  templateUrl: './account-edition.modal.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountEditionModal implements OnInit {

  resourceCodeSelected = input<string | null>();
  close = output<void>();

  accountData = signal<AccountResponseDTO | null>(null);
  accountState = accountState;

  formAccount!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private notificationService: NotificationService
  ) { }

  ngOnInit(): void {
    this.accountState.isLoadingSendingAccount.set(false);
    this.accountState.errorSendingAccount.set(null);

    this.loadAccountData()
    this.initForms();
  }

  get isEditMode(): boolean {
    return this.resourceCodeSelected() !== null;
  }

  onSubmit() {
    if (this.formAccount.invalid) return;

    this.accountState.isLoadingSendingAccount.set(true);
    this.accountState.errorSendingAccount.set(null);

    const accountData = this.buildAccountData();

    const observable = this.isEditMode
      ? this.accountService.update(this.resourceCodeSelected()!, accountData)
      : this.accountService.create(accountData);

    observable.subscribe({
      next: (response: ApiResponseDTO<AccountResponseDTO>) => {
        this.accountState.isLoadingSendingAccount.set(false);

        if (response.success && response.data) {
          this.notificationService.success(`Cuenta ${this.isEditMode ? 'actualizada' : 'creada'} con éxito.`);
          this.accountService.refresh();
          this.close.emit();
        } else {
          this.accountState.errorSendingAccount.set(response.message);
        }
      }, error: () => {
        this.accountState.errorSendingAccount.set('Error guardando la cuenta. Por favor, inténtalo de nuevo más tarde.');
        this.accountState.isLoadingSendingAccount.set(false);
      }
    });
  }

  onClose(): void {
    this.close.emit();
  }

  retryLoadAccountData(): void {
    this.loadAccountData();
  }

  private buildAccountData(): AccountRequestDTO {
    const formValues = this.formAccount.value;
    const accountName = formValues.name;
    const initialBalance = this.isEditMode ? null : formValues.initialBalance;

    return new AccountRequestDTO(accountName, initialBalance);

  }

  private initForms(): void {
    this.formAccount = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(150)]],
      initialBalance: [0, [Validators.required, Validators.min(0)]],
    });

    const initialBalanceControl = this.formAccount.get('initialBalance');
    if (this.isEditMode) {
      initialBalanceControl?.clearValidators();
      initialBalanceControl?.updateValueAndValidity();
    }
  }

  private loadAccountData(): void {
    if (!this.isEditMode) return;

    this.accountState.isLoadingGetAccount.set(true);

    this.accountService.getByResourceCode(this.resourceCodeSelected()!).subscribe({
      next: (response: ApiResponseDTO<AccountResponseDTO>) => {
        if (response.success && response.data) {
          this.accountData.set(response.data);
          this.formAccount.patchValue({
            name: response.data.name,
          });
        } else {
          console.error('Error fetching account data:', response.message);
          this.accountState.errorGetAccount.set('Error fetching account data: ' + response.message);
        }
        this.accountState.isLoadingGetAccount.set(false);
      },
      error: (error) => {
        console.error('Error fetching account data:', error);
        this.accountState.errorGetAccount.set(error.message || 'Error fetching account data');
        this.accountState.isLoadingGetAccount.set(false);
      }
    });
  }
}

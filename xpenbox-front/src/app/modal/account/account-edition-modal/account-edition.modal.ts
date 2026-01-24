import { Component, ChangeDetectionStrategy, output, input, OnInit, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AccountService } from '../../../feature/account/service/account.service';
import { AccountRequestDTO } from '../../../feature/account/model/account.request.dto';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { AccountResponseDTO } from '../../../feature/account/model/account.response.dto';


@Component({
  selector: 'app-account-edition-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './account-edition.modal.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountEditionModal implements OnInit {

  resourceCodeSelected = input<string | null>();
  close = output<void>();

  accountData = signal<AccountResponseDTO | null>(null);

  formAccount!: FormGroup;
  loading = signal<boolean>(false);
  sendingForm = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService
  ) { }

  ngOnInit(): void {
    this.loadAccountData()
    this.initForms();
  }

  get isEditMode(): boolean {
    return this.resourceCodeSelected() !== null;
  }

  onSubmit() {
    if (this.formAccount.invalid) return;

    this.sendingForm.set(true);
    this.errorMessage.set(null);

    const accountData = this.buildAccountData();

    const observable = this.isEditMode
      ? this.accountService.update(this.resourceCodeSelected()!, accountData)
      : this.accountService.create(accountData);

    observable.subscribe({
      next: (response: ApiResponseDTO<AccountResponseDTO>) => {
        this.sendingForm.set(false);

        if (response.success && response.data) {
          this.accountService.refresh();
          this.close.emit();
        } else {
          console.error('Error creating account:', response.message);
          this.errorMessage.set('Error creating account: ' + response.message);
        }
      }, error: (error) => {
        console.error('Error creating account:', error);
        this.errorMessage.set(error.message || 'Error creating account');
        this.sendingForm.set(false);
      }
    });
  }

  onClose(): void {
    this.close.emit();
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

    this.loading.set(true);

    this.accountService.getByResourceCode(this.resourceCodeSelected()!).subscribe({
      next: (response: ApiResponseDTO<AccountResponseDTO>) => {
        if (response.success && response.data) {
          this.accountData.set(response.data);
          this.formAccount.patchValue({
            name: response.data.name,
          });
        } else {
          console.error('Error fetching account data:', response.message);
          this.errorMessage.set('Error fetching account data: ' + response.message);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error fetching account data:', error);
        this.errorMessage.set(error.message || 'Error fetching account data');
        this.loading.set(false);
      }
    });
  }
}

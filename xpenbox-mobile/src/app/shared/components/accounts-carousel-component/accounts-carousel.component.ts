import { Component, effect, input, output, signal } from '@angular/core';
import { AccountCreditDTO } from '../../dto/account-credit.dto';
import { CommonModule } from '@angular/common';
import { LoadingUi } from '../../ui/loading-ui/loading.ui';

@Component({
  selector: 'app-accounts-carousel-component',
  imports: [CommonModule, LoadingUi],
  templateUrl: './accounts-carousel.component.html',
  styleUrl: './accounts-carousel.component.css',
  host: {
    'class': 'block',
    'ngSkipHydration': 'true'
  }
})
export class AccountsCarouselComponent {

  // Input/Output signals
  isLoading = input<boolean>();
  errorLoading = input<string | null>();
  carouselTitle = input<string>();
  carouselErrorMessage = input<string>();
  currencySymbol = input<string>();
  accountsList = input<AccountCreditDTO[]>();
  currentSelectedAccount = input<AccountCreditDTO | null>();
  noAccountsMessage = input<string>();

  selectedAccountOutput = output<AccountCreditDTO>();
  retry = output<void>();

  selectedAccount = signal<AccountCreditDTO | null>(null);

  constructor() {
    // Initialize selected account with the current selected account input
    this.selectedAccount.set(this.currentSelectedAccount()!);

    // Effect to update selectedAccount when currentSelectedAccount input changes
    effect(() => {
      this.selectedAccount.set(this.currentSelectedAccount()!);
    });
  }

  /**
   * Select an account
   * @param account The account to select
   * @returns void
   */
  selectAccount(account: AccountCreditDTO): void {
    this.selectedAccount.set(account);
    this.selectedAccountOutput.emit(account);
  }

  /**
   * Check if an account is selected
   * @param accountResourceCode The resource code of the account to check
   * @returns boolean
   */
  isSelectedAccount(accountResourceCode: string): boolean {
    return this.selectedAccount()?.resourceCode === accountResourceCode;
  }

  /** Emit retry event */
  sendRetry(): void {
    this.retry.emit();
  }
}

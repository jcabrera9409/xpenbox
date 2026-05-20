import { Component, effect, OnInit, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { categoryState } from '../../../feature/category/service/category.state';
import { accountState } from '../../../feature/account/service/account.state';
import { creditCardState } from '../../../feature/creditcard/service/creditcard.state';
import { AccountService } from '../../../feature/account/service/account.service';
import { CreditCardService } from '../../../feature/creditcard/service/creditcard.service';
import { CategoryResponseDTO } from '../../../feature/category/model/category.response.dto';
import { AccountCreditDTO, AccountCreditType } from '../../../shared/dto/account-credit.dto';
import { TransactionRequestDTO } from '../../../feature/transaction/model/transaction.request.dto';
import { TransactionService } from '../../../feature/transaction/service/transaction.service';
import { transactionState } from '../../../feature/transaction/service/transaction.state';
import { AccountsCarouselComponent } from '../../../shared/components/accounts-carousel-component/accounts-carousel.component';
import { AccountCreditService } from '../../../shared/service/account-credit.service';
import { CategoriesCarouselComponent } from '../../../shared/components/categories-carousel-component/categories-carousel.component';
import { CategoryService } from '../../../feature/category/service/category.service';
import { DateService } from '../../../shared/service/date.service';
import { userState } from '../../../feature/user/service/user.state';
import { IconComponent } from '../../../shared/components/icon.component/icon.component';
import { InputComponent } from '../../../shared/components/input-component/input.component';
import { InputAmountComponent } from '../../../shared/components/input-amount-component/input-amount-component';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GenericModal } from '../../common/generic-modal/generic.modal';

@Component({
  selector: 'app-quick-expense-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AccountsCarouselComponent, CategoriesCarouselComponent, IconComponent, InputComponent, InputAmountComponent, GenericModal],
  templateUrl: './quick-expense.modal.html',
  styleUrl: './quick-expense.modal.css',
})
export class QuickExpenseModal implements OnInit {

  userLogged = userState.userLogged;

  close = output<void>();

  // Application states
  categoryState = categoryState;
  accountState = accountState;
  creditCardState = creditCardState;  
  transactionState = transactionState;

  selectedCategory = signal<CategoryResponseDTO | null>(null);
  selectedAccount = signal<AccountCreditDTO | null>(null);
  accountCredits = signal<AccountCreditDTO[]>([]);
  assignToCategory = signal<boolean>(false);

  formExpense!: FormGroup;
  maxDate = signal('');
  amountOutput = signal<number>(0);

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private accountService: AccountService,
    private creditCardService: CreditCardService,
    private transactionService: TransactionService,
    private accountCreditService: AccountCreditService,
    private dateService: DateService
  ) {
    if (this.accountState.accounts().length === 0) {
      this.accountService.load();
    }
    if (this.creditCardState.creditCards().length === 0) {
      this.creditCardService.load();
    }

    // Combine accounts and credit cards into accountCredits
    effect(() => {
      const accounts = this.accountState.accounts();
      const creditCards = this.creditCardState.creditCards();

      if (!this.accountState.isLoadingGetList() && !this.creditCardState.isLoadingGetList()) {
        const accountCreditsList: AccountCreditDTO[] = this.accountCreditService.combineAccountAndCreditCardData(accounts, creditCards);

        const availableList = this.accountCreditService.filterAndSortAccountCredits(accountCreditsList, this.amountOutput());
        this.accountCredits.set(availableList);

        if (availableList.length > 0 && !this.selectedAccount()) {
          this.selectedAccount.set(availableList[0] || null);
        }
      }
    });

    // Update selected account when amount changes
    effect(() => {
      const amountValue = this.amountOutput();
      const accounts = this.accountCredits();
      const currentSelected = this.selectedAccount();
      
      // If amount is invalid, reset to first account
      if (isNaN(amountValue) || amountValue <= 0) {
        if (!currentSelected && accounts.length > 0) {
          this.selectedAccount.set(accounts[0]);
        }
        return;
      }
      
      // Check if the current account is still valid
      const isCurrentValid = currentSelected && currentSelected.balance >= amountValue;
      
      if (!isCurrentValid) {
        // Find a valid account that covers the amount`
        const validAccount = accounts.find(acc => acc.balance >= amountValue);
        this.selectedAccount.set(validAccount || null);
      }
    });
  } 

  ngOnInit(): void {
    this.transactionState.isLoadingSendingTransaction.set(false);
    this.transactionState.errorSendingTransaction.set(null);

    this.initForms();
  }

  // Getters for filtered and sorted lists
  get categories(): CategoryResponseDTO[] {
    return this.categoryState.categories().filter(c => c.state);
  }

  // Getters for form validity
  get isFormValid(): boolean {
    const amountValue = this.amountOutput();
    const selectedAccount = this.selectedAccount();
    const categorySelected = this.selectedCategory();

    // Check if the selected account covers the amount`
    const isAccountValid = selectedAccount !== null &&
      !isNaN(amountValue) &&
      amountValue > 0 &&
      selectedAccount.balance >= amountValue;
    
    // If assigning to category, ensure one is selected
    const assignToCat = this.assignToCategory();
    if (assignToCat && !categorySelected) {
      return false;
    }

    return isAccountValid && this.formExpense.valid;
  }

  get amountControl(): FormControl {
    return this.formExpense.get('amount') as FormControl;
  }

  get descriptionControl(): FormControl {
    return this.formExpense.get('description') as FormControl;
  }

  get transactionDateControl(): FormControl {
    return this.formExpense.get('transactionDate') as FormControl;
  }

  initForms(): void {
    const today = this.dateService.toTimestamp(this.dateService.getLocalDatetime());
    const formattedDate = this.dateService.format(today, 'ISO-LOCAL');
    this.maxDate.set(formattedDate);

    this.formExpense = this.fb.group({
      amount: [null, [
        Validators.required,
        Validators.min(0.01)
      ]],
      description: [''],
      transactionDate: [formattedDate, [Validators.required]],
    });
  }

  retryAccountsAndCreditCards(): void {
    this.accountService.refresh();
    this.creditCardService.refresh();
  }

  /**
   * Select a category
   * @param category The category to select
   * @returns void
   */
  selectCategory(category: CategoryResponseDTO): void {
    this.selectedCategory.set(category);
  }
  /**
   * Close the modal
   * @returns void
   */
  onClose(): void {
    this.close.emit();
  }

  /**
   * Save the transaction
   * @returns void
   */
  onSubmit(): void {
    if (!this.isFormValid) return;
    
    const amountValue = this.formExpense.get('amount')?.value / 100;
    const descriptionValue = this.formExpense.get('description')?.value;
    const transactionDate = this.dateService.parseDatetimeIsoString(this.transactionDateControl.value);
    const transactionDateTimestamp = this.dateService.toTimestamp(transactionDate);
    const selectedAccount = this.selectedAccount();
    const categorySelected = this.assignToCategory() ? this.selectedCategory() : null;

    this.transactionState.isLoadingSendingTransaction.set(true);
    
    const transactionRequest = selectedAccount?.type === AccountCreditType.ACCOUNT
      ? TransactionRequestDTO.generateExpenseAccountTransaction(amountValue, descriptionValue, selectedAccount?.resourceCode || '', categorySelected?.resourceCode, transactionDateTimestamp)
      : TransactionRequestDTO.generateExpenseCreditCardTransaction(amountValue, descriptionValue, selectedAccount?.resourceCode || '', categorySelected?.resourceCode, transactionDateTimestamp);
  
    this.transactionService.submitTransaction(transactionRequest, () => this.successTransactionCreated());
  }

  private successTransactionCreated(): void {
    this.close.emit();
    this.accountService.refresh();
    this.creditCardService.refresh();
    this.categoryService.refresh();
  }
}

import { Injectable } from '@angular/core';
import { AccountResponseDTO } from '../../feature/account/model/account.response.dto';
import { CreditCardResponseDTO } from '../../feature/creditcard/model/creditcard.response.dto';
import { AccountCreditDTO, AccountCreditType } from '../dto/account-credit.dto';

/**
 * Service for handling account and credit card data
 */
@Injectable({
  providedIn: 'root',
})
export class AccountCreditService {
  
  /**
   * Combine account and credit card data into a unified AccountCreditDTO list
   * @param accounts The list of account response DTOs
   * @param creditCards The list of credit card response DTOs
   * @returns A combined list of AccountCreditDTOs
   */
  combineAccountAndCreditCardData(accounts: AccountResponseDTO[], creditCards: CreditCardResponseDTO[]): AccountCreditDTO[] {
    const accountCreditsList: AccountCreditDTO[] = [
      ...accounts.map(acc => ({
        resourceCode: acc.resourceCode,
        type: AccountCreditType.ACCOUNT,
        icon: 'account_balance',
        name: acc.name,
        balance: acc.balance,
        lastUsedDateTimestamp: acc.lastUsedDateTimestamp,
        usageCount: acc.usageCount,
      })),
      ...creditCards.map(cc => ({
        resourceCode: cc.resourceCode,
        type: AccountCreditType.CREDIT_CARD,
        icon: 'credit_card',
        name: cc.name,
        balance: cc.creditLimit - cc.currentBalance,
        lastUsedDateTimestamp: cc.lastUsedDateTimestamp,
        usageCount: cc.usageCount,
      }))
    ];

    return accountCreditsList;
  }

  /**
   * Filter and sort account credits to have the two most recently used at the top,
   * followed by the two most used, and then the rest sorted by type and balance.
   * @param accountCreditsList The list of account credits to filter and sort.
   * @returns The filtered and sorted list of account credits.
   */
  filterAndSortAccountCredits(accountCreditsList: AccountCreditDTO[], amountFilter: number): AccountCreditDTO[] {
    const amountValue = amountFilter;
    const filtered = accountCreditsList.filter(ac => ac.balance > amountValue && ac.balance >= (isNaN(amountValue) ? 0 : amountValue));

    const sortedByLastUsed = [...filtered]
      .filter(ac => ac.lastUsedDateTimestamp)
      .sort((a, b) => (b.lastUsedDateTimestamp || 0) - (a.lastUsedDateTimestamp || 0));
    const lastTwo = sortedByLastUsed.slice(0, 2);
    const lastTwoIds = new Set(lastTwo.map(ac => ac.resourceCode));

    const restAfterLastTwo = filtered.filter(ac => !lastTwoIds.has(ac.resourceCode));
    const sortedByUsage = [...restAfterLastTwo]
      .sort((a, b) => (b.usageCount || 0) - (a.usageCount || 0));
    const mostUsedTwo = sortedByUsage.slice(0, 2);
    const mostUsedTwoIds = new Set(mostUsedTwo.map(ac => ac.resourceCode));

    const rest = restAfterLastTwo.filter(ac => !mostUsedTwoIds.has(ac.resourceCode))
      .sort((a, b) => {
        if (a.type === b.type) {
          return b.balance - a.balance;
        }
        return a.type === AccountCreditType.ACCOUNT ? -1 : 1;
      });

    return [...lastTwo, ...mostUsedTwo, ...rest];
  }
}

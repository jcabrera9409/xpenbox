import { signal } from "@angular/core";
import { AccountResponseDTO } from "../model/account.response.dto";

/**
 * State management for account-related data.
 * @returns An object containing signals for loading state, error messages, account list, and total liquid balance.
 */
export const accountState = {
    accounts: signal<AccountResponseDTO[]>([]),
    totalLiquidBalance: signal<number>(0),

    isLoadingGetList: signal<boolean>(false),
    errorGetList: signal<string | null>(null),

    isLoadingGetAccount: signal<boolean>(false),
    errorGetAccount: signal<string | null>(null),

    isLoadingSendingAccount: signal<boolean>(false),
    errorSendingAccount: signal<string | null>(null)
};
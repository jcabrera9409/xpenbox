import { signal } from "@angular/core";
import { AccountResponseDTO } from "../model/account.response.dto";

/**
 * State management for account-related data.
 * @returns An object containing signals for loading state, error messages, account list, and total liquid balance.
 */
export const accountState = {
    isLoading: signal<boolean>(false),
    error: signal<string | null>(null),
    accounts: signal<AccountResponseDTO[]>([]),
    totalLiquidBalance: signal<number>(0)
};
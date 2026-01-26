import { signal } from "@angular/core";
import { TransactionResponseDTO } from "../model/transaction.response.dto";

/**
 * State management for transactions.
 * Includes loading status, error messages, and the list of transactions.
 */
export const transactionState = {
    isLoading: signal<boolean>(false),
    error: signal<string | null>(null),
    transactions: signal<TransactionResponseDTO[]>([]),
    isSuccess: signal<boolean>(false),
};
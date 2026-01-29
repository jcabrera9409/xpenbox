import { signal } from "@angular/core";
import { TransactionResponseDTO } from "../model/transaction.response.dto";
import { TransactionFilterRequestDTO } from "../model/transaction-filter.request.dto";
import { PageableResponseDTO } from "../../common/model/pageable.response.dto";

/**
 * State management for transactions.
 * Includes loading status, error messages, and the list of transactions.
 */
export const transactionState = {
    transactions: signal<TransactionResponseDTO[]>([]),
    transactionsFiltered: signal<PageableResponseDTO<TransactionResponseDTO> | null>(null),
    
    isLoadingFilteredList: signal<boolean>(false),
    errorFilteredList: signal<string | null>(null),

    isLoadingGetList: signal<boolean>(false),
    errorGetList: signal<string | null>(null),
    
    isLoadingGetTransaction: signal<boolean>(false),
    errorGetTransaction: signal<string | null>(null),
    
    isLoadingSendingTransaction: signal<boolean>(false),
    successSendingTransaction: signal<boolean>(false),
    errorSendingTransaction: signal<string | null>(null)
};
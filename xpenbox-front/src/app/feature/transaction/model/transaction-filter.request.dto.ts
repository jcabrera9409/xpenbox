import { TransactionType } from "./transaction.request.dto";

/**
 * Data Transfer Object for filtering transactions.
 * Includes optional fields for various filter criteria.
 */
export class TransactionFilterRequestDTO {
    resourceCode?: string;
    transactionType?: TransactionType;
    description?: string;
    transactionDateTimestampFrom?: number;
    transactionDateTimestampTo?: number;
    categoryResourceCode?: string;
    incomeResourceCode?: string;
    accountResourceCode?: string;
    creditCardResourceCode?: string;
    pageNumber?: number;
    pageSize?: number;

    /**
     * Creates an empty TransactionFilterRequestDTO instance.
     * @returns {TransactionFilterRequestDTO} An instance with no fields set.
     */
    static createEmpty(): TransactionFilterRequestDTO {
        const dto = new TransactionFilterRequestDTO();
        dto.pageNumber = 0;
        dto.pageSize = 10;
        return dto;
    }
}
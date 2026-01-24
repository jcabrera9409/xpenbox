/**
 * Data Transfer Object for Income Response
 * Includes details about the income entry such as resource code, concept, date, and total amount.
 */
export interface IncomeResponseDTO {
    resourceCode: string;
    concept: string;
    incomeDateTimestamp: number;
    totalAmount: number;
}
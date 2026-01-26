/**
 * Data Transfer Object for Income Response
 * Includes details about the income entry such as resource code, concept, date, total amount, and allocated amount.
 */
export interface IncomeResponseDTO {
    resourceCode: string;
    concept: string;
    incomeDateTimestamp: number;
    totalAmount: number;
    allocatedAmount: number;
}
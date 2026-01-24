/**
 * Data Transfer Object representing the response structure for a credit card.
 * Includes resource code, name, credit limit, current balance, last used date timestamp, usage count, state, billing day, payment day, and closing date timestamp.
 */
export interface CreditCardResponseDTO {
    resourceCode: string;
    name: string;
    creditLimit: number;
    currentBalance: number;
    lastUsedDateTimestamp: number;
    usageCount: number;
    state: boolean;
    billingDay: number;
    paymentDay: number;
    closingDateTimestamp: number;
}
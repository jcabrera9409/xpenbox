/**
 * Data Transfer Object representing the response structure for a credit card.
 */
export interface CreditCardResponseDTO {
    resourceCode: string;
    name: string;
    creditLimit: number;
    currentBalance: number;
    state: boolean;
    billingDay: number;
    paymentDay: number;
    closingDateTimestamp: number;
}
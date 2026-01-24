/**
 * Data Transfer Object for Credit Card requests.
 * Includes properties for credit card details.
 */
export class CreditCardRequestDTO {
    name: string | null;
    creditLimit: number | null;
    currentBalance: number | null;
    billingDay: number | null;
    paymentDay: number | null;
    state: boolean | null;

    constructor(
        name: string | null,
        creditLimit: number | null,
        currentBalance: number | null,
        billingDay: number | null,
        paymentDay: number | null,
        state: boolean | null
    ) {
        this.name = name;
        this.creditLimit = creditLimit;
        this.currentBalance = currentBalance;
        this.billingDay = billingDay;
        this.paymentDay = paymentDay;
        this.state = state;
    }
}
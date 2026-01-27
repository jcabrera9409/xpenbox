/**
 * Data Transfer Object for Income Request
 * This class encapsulates the data required to create or update an income entry.
 */
export class IncomeRequestDTO {
    concept: string;
    incomeDateTimestamp: number;
    totalAmount: number;
    accountResourceCode?: string;

    constructor(
        concept: string,
        incomeDateTimestamp: number,
        totalAmount: number,
        accountResourceCode?: string
    ) {
        this.concept = concept;
        this.incomeDateTimestamp = incomeDateTimestamp;
        this.totalAmount = totalAmount;
        this.accountResourceCode = accountResourceCode;
    }
}
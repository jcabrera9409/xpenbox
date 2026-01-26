/**
 * Data Transfer Object for Income Request
 * This class encapsulates the data required to create or update an income entry.
 */
export class IncomeRequestDTO {
    concept: string;
    incomeDateTimestamp: string;
    totalAmount: number;

    constructor(
        concept: string,
        incomeDateTimestamp: string,
        totalAmount: number
    ) {
        this.concept = concept;
        this.incomeDateTimestamp = incomeDateTimestamp;
        this.totalAmount = totalAmount;
    }
}
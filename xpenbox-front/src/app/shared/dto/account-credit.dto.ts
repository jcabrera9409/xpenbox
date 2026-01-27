/**
 * Data Transfer Object representing an account credit.
 * Includes details such as resource code, type, icon, name, balance,
 * last used date timestamp, and usage count.
 */
export interface AccountCreditDTO {
    resourceCode: string;
    type: AccountCreditType;
    icon: string;
    name: string;
    balance: number;
    lastUsedDateTimestamp: number;
    usageCount: number;
}

/**
 * Enumeration for the type of account credit.
 */
export enum AccountCreditType {
    ACCOUNT = 'Débito',
    CREDIT_CARD = 'Crédito',
}
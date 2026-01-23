
export interface AccountCreditDTO {
    resourceCode: string;
    type: AccountCreditType;
    icon: string;
    name: string;
    balance: number;
}

export enum AccountCreditType {
    ACCOUNT = 'Débito',
    CREDIT_CARD = 'Crédito',
}
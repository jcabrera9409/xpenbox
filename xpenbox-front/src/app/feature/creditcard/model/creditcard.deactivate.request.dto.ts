
export class CreditCardDeactivateRequestDTO {
    accountResourceCode?: string;

    constructor(accountResourceCode: string | undefined) {
        this.accountResourceCode = accountResourceCode;
    }
}
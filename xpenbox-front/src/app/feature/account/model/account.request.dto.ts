/**
 * Data Transfer Object for Account requests.
 * Includes the account name and an optional balance.
 */
export class AccountRequestDTO {
    name: string;
    balance: number | null;

    constructor(name: string, balance: number | null) {
        this.name = name;
        this.balance = balance;
    }
}
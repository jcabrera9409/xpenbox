
/**
 * Data Transfer Object for account deactivation request.
 * Includes the resource code of the account to which remaining balance should be transferred.
 */
export class AccountDeactivateRequestDTO {
    accountResourceCodeToTransfer?: string;

    constructor(accountResourceCodeToTransfer: string | undefined) {
        this.accountResourceCodeToTransfer = accountResourceCodeToTransfer;
    }
}
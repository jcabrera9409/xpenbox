/**
 * DTO representing the response structure for an account.
 * Includes resource code, name, balance, state, last used date timestamp, usage count, and optional closing date timestamp.
 */
export interface AccountResponseDTO {
    resourceCode: string;
    name: string;
    balance: number;
    lastUsedDateTimestamp: number;
    usageCount: number;
    state: boolean;
    closingDateTimestamp: number | null;
}
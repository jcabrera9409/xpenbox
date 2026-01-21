import { signal } from "@angular/core";
import { AccountResponseDTO } from "../model/accountResponseDTO";

export const accountState = {
    isLoading: signal<boolean>(false),
    error: signal<string | null>(null),
    accounts: signal<AccountResponseDTO[]>([]),
    totalLiquidBalance: signal<number>(0)
};
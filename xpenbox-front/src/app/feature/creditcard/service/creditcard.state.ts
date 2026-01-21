import { signal } from "@angular/core";
import { CreditCardResponseDTO } from "../model/creditCardResponseDTO";

export const creditCardState = {
    isLoading: signal<boolean>(false),
    error: signal<string | null>(null),
    creditCards: signal<CreditCardResponseDTO[]>([]),
    totalCreditBalance: signal<number>(0)
};
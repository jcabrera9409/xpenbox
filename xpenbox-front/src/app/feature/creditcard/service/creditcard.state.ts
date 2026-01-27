import { signal } from "@angular/core";
import { CreditCardResponseDTO } from "../model/creditcard.response.dto";

export const creditCardState = {
    creditCards: signal<CreditCardResponseDTO[]>([]),
    totalCreditBalance: signal<number>(0),

    isLoadingGetList: signal<boolean>(false),
    errorGetList: signal<string | null>(null),

    isLoadingGetCreditCard: signal<boolean>(false),
    errorGetCreditCard: signal<string | null>(null),

    isLoadingSendingCreditCard: signal<boolean>(false),
    errorSendingCreditCard: signal<string | null>(null)
};
import { signal } from "@angular/core";
import { IncomeResponseDTO } from "../model/income.response.dto";

export const incomeState = {
    incomes: signal<IncomeResponseDTO[]>([]),

    isLoadingGetList: signal<boolean>(false),
    errorGetList: signal<string | null>(null),

    isLoadingGetIncome: signal<boolean>(false),
    errorGetIncome: signal<string | null>(null),

    isLoadingSendingIncome: signal<boolean>(false),
    errorSendingIncome: signal<string | null>(null),

    // Derived signal to filter incomes by date range
    startDate: signal<Date | null>(null),
    endDate: signal<Date | null>(null),
};
import { signal } from "@angular/core";
import { IncomeResponseDTO } from "../model/income.response.dto";

export const incomeState = {
    isLoading: signal<boolean>(false),
    error: signal<string | null>(null),
    incomes: signal<IncomeResponseDTO[]>([]),
};
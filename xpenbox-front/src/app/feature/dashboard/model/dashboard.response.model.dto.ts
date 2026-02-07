import { CategoryResponseDTO } from "../../category/model/category.response.dto";
import { CreditCardResponseDTO } from "../../creditcard/model/creditcard.response.dto";
import { TransactionResponseDTO } from "../../transaction/model/transaction.response.dto";

export interface DashboardResponseModelDTO {
    current: DashboardCurrentPeriodDTO;
    period: DashboardPeriodFilterDTO;
}

export interface DashboardCurrentPeriodDTO {
    currentBalance: number;
    openingBalance: number;
    deltaBalance: number;
    creditUsed: number;
    creditLimit: number;
    creditCards: CreditCardResponseDTO[];
}

export interface DashboardPeriodFilterDTO {
    incomeTotal: number;
    expenseTotal: number;
    netCashFlow: number;
    categories: CategoryResponseDTO[];
    lastTransactions: TransactionResponseDTO[];
}
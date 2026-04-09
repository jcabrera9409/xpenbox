import { AccountResponseDTO } from "../../account/model/account.response.dto";
import { CategoryResponseDTO } from "../../category/model/category.response.dto";
import { CreditCardResponseDTO } from "../../creditcard/model/creditcard.response.dto";
import { IncomeResponseDTO } from "../../income/model/income.response.dto";
import { TransactionType } from "./transaction.request.dto";

export interface TransactionResponseDTO {
    resourceCode: string;
    description: string;
    transactionType: TransactionType;
    amount: number;
    latitude?: number;
    longitude?: number;
    transactionDateTimestamp: number;
    category?: CategoryResponseDTO;
    income?: IncomeResponseDTO;
    account?: AccountResponseDTO;
    creditCard?: CreditCardResponseDTO;
    destinationAccount?: AccountResponseDTO;
}

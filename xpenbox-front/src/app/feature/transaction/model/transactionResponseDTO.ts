import { AccountResponseDTO } from "../../account/model/accountResponseDTO";
import { CategoryResponseDTO } from "../../category/model/categoryResponseDTO";
import { CreditCardResponseDTO } from "../../creditcard/model/creditCardResponseDTO";
import { TransactionType } from "./transactionRequestDTO";

export interface TransactionResponseDTO {
    resourceCode: string;
    description: string;
    transactionType: TransactionType;
    amount: number;
    latitude?: number;
    longitude?: number;
    transactionDateTimestamp: number;
    category?: CategoryResponseDTO;
    income?: AccountResponseDTO;
    account?: AccountResponseDTO;
    creditCard?: CreditCardResponseDTO;
    destinationAccount?: AccountResponseDTO;
}

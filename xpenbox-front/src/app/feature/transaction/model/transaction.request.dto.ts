export class TransactionRequestDTO {
    transactionType?: TransactionType;
    description?: string;
    amount?: number;
    latitude?: number;
    longitude?: number;
    transactionDateTimestamp?: number;
    categoryResourceCode?: string;
    incomeResourceCode?: string;
    accountResourceCode?: string;
    creditCardResourceCode?: string;
    destinationAccountResourceCode?: string;

    static generateExpenseAccountTransaction(amount: number, description: string, accountResourceCode: string, categoryResourceCode?: string, transactionDateTimestamp?: number): TransactionRequestDTO {
        const transaction = new TransactionRequestDTO();
        transaction.transactionType = TransactionType.EXPENSE;
        transaction.amount = amount;
        transaction.description = description;
        transaction.transactionDateTimestamp = transactionDateTimestamp || Date.now();
        transaction.accountResourceCode = accountResourceCode;
        transaction.categoryResourceCode = categoryResourceCode;

        return transaction;
    }

    static generateExpenseCreditCardTransaction(amount: number, description: string, creditCardResourceCode: string, categoryResourceCode?: string, transactionDateTimestamp?: number): TransactionRequestDTO {
        const transaction = new TransactionRequestDTO();
        transaction.transactionType = TransactionType.EXPENSE;
        transaction.amount = amount;
        transaction.description = description;
        transaction.transactionDateTimestamp = transactionDateTimestamp || Date.now();
        transaction.creditCardResourceCode = creditCardResourceCode;
        transaction.categoryResourceCode = categoryResourceCode;

        return transaction;
    }

    static generateIncomeAssignmentTransaction(amount: number, description: string, incomeResourceCode: string, accountResourceCode: string, transactionDateTimestamp?: number): TransactionRequestDTO {
        const transaction = new TransactionRequestDTO();
        transaction.transactionType = TransactionType.INCOME;
        transaction.amount = amount;
        transaction.description = description;
        transaction.transactionDateTimestamp = transactionDateTimestamp || Date.now();
        transaction.incomeResourceCode = incomeResourceCode;
        transaction.accountResourceCode = accountResourceCode;
        return transaction;
    }
}

export enum TransactionType {
    INCOME = 'INCOME',
    EXPENSE = 'EXPENSE',
    TRANSFER = 'TRANSFER',
    CREDIT_PAYMENT = 'CREDIT_PAYMENT'
}
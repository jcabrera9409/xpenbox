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

    static generateTransferTransaction(amount: number, description: string, originAccountResourceCode: string, destinationAccountResourceCode: string, transactionDateTimestamp?: number): TransactionRequestDTO {
        const transaction = new TransactionRequestDTO();
        transaction.transactionType = TransactionType.TRANSFER;
        transaction.amount = amount;
        transaction.description = description;
        transaction.transactionDateTimestamp = transactionDateTimestamp || Date.now();
        transaction.accountResourceCode = originAccountResourceCode;
        transaction.destinationAccountResourceCode = destinationAccountResourceCode;
        return transaction;
    }

    static generateCreditCardPaymentTransaction(amount: number, description: string, creditCardResourceCode: string, accountResourceCode: string, categoryResourceCode?: string, transactionDateTimestamp?: number): TransactionRequestDTO {
        const transaction = new TransactionRequestDTO();
        transaction.transactionType = TransactionType.CREDIT_PAYMENT;
        transaction.amount = amount;
        transaction.description = description;
        transaction.transactionDateTimestamp = transactionDateTimestamp || Date.now();
        transaction.creditCardResourceCode = creditCardResourceCode;
        transaction.accountResourceCode = accountResourceCode;
        transaction.categoryResourceCode = categoryResourceCode;
        return transaction;
    }
}

export enum TransactionType {
    ALL = 'ALL',
    INCOME = 'INCOME',
    EXPENSE = 'EXPENSE',
    TRANSFER = 'TRANSFER',
    CREDIT_PAYMENT = 'CREDIT_PAYMENT',
}

export namespace TransactionType {
    export function getLabel(type: TransactionType | undefined): string {
        switch (type) {
            case TransactionType.INCOME:
                return 'Ingreso';
            case TransactionType.EXPENSE:
                return 'Gasto';
            case TransactionType.TRANSFER:
                return 'Transferencia';
            case TransactionType.CREDIT_PAYMENT:
                return 'Pago de Crédito';
            case TransactionType.ALL:
                return 'Todos';
            default:
                return 'Desconocido';
        }
    }

    export function getTransactionBgColorClass(type: TransactionType | undefined): string {
        switch (type) {
        case TransactionType.INCOME:
            return 'xpb-income';
        case TransactionType.EXPENSE:
            return 'xpb-expense';
        case TransactionType.TRANSFER:
            return 'xpb-transfer';
        case TransactionType.CREDIT_PAYMENT:
            return 'xpb-credit';
        default:
            return 'xpb-disabled';
        } 
    }

    export function getTransactionIcon(type: TransactionType | undefined): string {
        switch (type) {
        case TransactionType.INCOME:
            return 'trending_up';
        case TransactionType.EXPENSE:
            return 'receipt_long';
        case TransactionType.TRANSFER:
            return 'swap_horiz';
        case TransactionType.CREDIT_PAYMENT:
            return 'payment';
        default:
            return 'help_outline';
        } 
    }
}
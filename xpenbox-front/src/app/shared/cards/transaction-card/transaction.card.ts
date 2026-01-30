import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransactionResponseDTO } from '../../../feature/transaction/model/transaction.response.dto';
import { TransactionType } from '../../../feature/transaction/model/transaction.request.dto';
import { DateService } from '../../service/date.service';

@Component({
  selector: 'app-transaction-card',
  imports: [CommonModule],
  templateUrl: './transaction.card.html',
  styleUrl: './transaction.card.css',
})
export class TransactionCard {

  transaction = input<TransactionResponseDTO>();
  currency = input<string>();
  detailView = output<string>();
  editView = output<string>();
  deleteView = output<string>();

  transactionType = TransactionType;

  constructor(
    private dateService: DateService
  ) { }

  getTransactionLabel(type: TransactionType | undefined): string {
    switch (type) {
      case TransactionType.INCOME:
        return 'Ingreso';
      case TransactionType.EXPENSE:
        return 'Gasto';
      case TransactionType.TRANSFER:
        return 'Transferencia';
      case TransactionType.CREDIT_PAYMENT:
        return 'Pago de Crédito';
      default:
        return 'Desconocido';
    }
  }

  getTransactionColorClass(type: TransactionType | undefined): string {
    switch (type) {
      case TransactionType.INCOME:
        return 'xpb-text-income';
      case TransactionType.EXPENSE:
        return 'xpb-text-expense';
      case TransactionType.TRANSFER:
        return 'xpb-text-primary';
      case TransactionType.CREDIT_PAYMENT:
        return 'xpb-text-credit';
      default:
        return 'xpb-text-disabled';
    } 
  }

  getTransactionBgColorClass(type: TransactionType | undefined): string {
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

  getFormatDate(dateTimestamp: number | undefined): string {
    const date = this.dateService.toDate(dateTimestamp || 0);

    const dateStr = this.dateService.format(date.getTime(), 'datetime');
    return dateStr;
  }

  getFormatAmount(amount: number | undefined): string {
    return (amount || 0).toLocaleString('es-ES', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }

  sendDetailView() {
    this.detailView.emit(this.transaction()?.resourceCode!);
  }
  
  sendEditView() {
    this.editView.emit(this.transaction()?.resourceCode!);
  }

  sendDeleteView() {
    this.deleteView.emit(this.transaction()?.resourceCode!);
  }
}

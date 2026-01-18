package org.xpenbox.transaction.service;

import org.xpenbox.common.service.IGenericService;
import org.xpenbox.transaction.dto.TransactionCreateDTO;
import org.xpenbox.transaction.dto.TransactionResponseDTO;
import org.xpenbox.transaction.dto.TransactionUpdateDTO;
import org.xpenbox.transaction.entity.Transaction;

/**
 * Service interface for managing transactions.
 */
public interface ITransactionService extends IGenericService<Transaction, TransactionCreateDTO, TransactionUpdateDTO, TransactionResponseDTO> {
    
}

package org.xpenbox.transaction.service;

import org.xpenbox.common.service.IGenericService;
import org.xpenbox.transaction.dto.TransactionCreateDTO;
import org.xpenbox.transaction.dto.TransactionResponseDTO;
import org.xpenbox.transaction.entity.Transaction;

/**
 * Service interface for managing transactions.
 */
public interface ITransactionService extends IGenericService<Transaction, TransactionCreateDTO, TransactionCreateDTO, TransactionResponseDTO> {
    
    /**
     * Rollbacks a transaction by its resource code.
     * @param resourceCode The resource code of the transaction to rollback.
     * @param userEmail The email of the user requesting the rollback.
     */
    void rollback(String resourceCode, String userEmail);
}

package org.xpenbox.transaction.service;

import org.xpenbox.common.dto.APIPageableDTO;
import org.xpenbox.common.service.IGenericService;
import org.xpenbox.transaction.dto.TransactionCreateDTO;
import org.xpenbox.transaction.dto.TransactionFilterDTO;
import org.xpenbox.transaction.dto.TransactionResponseDTO;
import org.xpenbox.transaction.dto.TransactionUpdateDTO;
import org.xpenbox.transaction.entity.Transaction;

/**
 * Service interface for managing transactions.
 */
public interface ITransactionService extends IGenericService<Transaction, TransactionCreateDTO, TransactionUpdateDTO, TransactionResponseDTO> {
    
    /**
     * Rollbacks a transaction by its resource code.
     * @param resourceCode The resource code of the transaction to rollback.
     * @param userEmail The email of the user requesting the rollback.
     */
    void rollback(String resourceCode, String userEmail);

    /**
     * Filters transactions based on the provided filter criteria.
     * @param filterDTO The DTO containing filter criteria.
     * @param userEmail The email of the user requesting the filtered transactions.
     * @return A pageable DTO containing the filtered transactions.
     */
    APIPageableDTO<TransactionResponseDTO> filterTransactions(TransactionFilterDTO filterDTO, String userEmail);
}

package org.xpenbox.transaction.mapper;

import org.jboss.logging.Logger;
import org.xpenbox.account.mapper.AccountMapper;
import org.xpenbox.category.mapper.CategoryMapper;
import org.xpenbox.creditcard.mapper.CreditCardMapper;
import org.xpenbox.income.mapper.IncomeMapper;
import org.xpenbox.transaction.dto.TransactionCreateDTO;
import org.xpenbox.transaction.dto.TransactionResponseDTO;
import org.xpenbox.transaction.entity.Transaction;

/**
 * Mapper class for converting between Transaction entities and DTOs.
 */
public class TransactionMapper {
    private static final Logger LOG = Logger.getLogger(TransactionMapper.class);

    /**
     * Maps Transaction entity to TransactionResponseDTO.
     * @param entity The Transaction entity to be mapped.
     * @return The corresponding TransactionResponseDTO.
     */
    public static TransactionResponseDTO toDTO(Transaction entity) {
        LOG.infof("Mapping Transaction entity to TransactionResponseDTO: %s", entity);
        TransactionResponseDTO dto = new TransactionResponseDTO(
            entity.getResourceCode(),
            entity.getDescription(),
            entity.getTransactionType(),
            entity.getAmount(),
            entity.getLatitude(),
            entity.getLongitude(),
            entity.getTransactionDate(),
            entity.getCategory() != null ? CategoryMapper.toDTO(entity.getCategory()) : null,
            entity.getIncome() != null ? IncomeMapper.toDTO(entity.getIncome()) : null,
            entity.getAccount() != null ? AccountMapper.toDTO(entity.getAccount()) : null,
            entity.getCreditCard() != null ? CreditCardMapper.toDTO(entity.getCreditCard()) : null,
            entity.getDestinationAccount() != null ? AccountMapper.toDTO(entity.getDestinationAccount()) : null
        );
        return dto;
    }

    /**
     * Maps TransactionCreateDTO to Transaction entity.
     * @param dto The TransactionCreateDTO to be mapped.
     * @return The corresponding Transaction entity.
     */
    public static Transaction toEntity(TransactionCreateDTO dto) {
        LOG.infof("Mapping TransactionResponseDTO to Transaction entity: %s", dto);
        Transaction entity = new Transaction();
        entity.setDescription(dto.description());
        entity.setAmount(dto.amount());
        entity.setLatitude(dto.latitude());
        entity.setLongitude(dto.longitude());
        entity.setTransactionDate(dto.transactionDate());
        return entity;
    }
}

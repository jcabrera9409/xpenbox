package org.xpenbox.transaction.mapper;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.account.mapper.AccountMapper;
import org.xpenbox.category.mapper.CategoryMapper;
import org.xpenbox.common.ResourceCode;
import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.creditcard.mapper.CreditCardMapper;
import org.xpenbox.income.mapper.IncomeMapper;
import org.xpenbox.transaction.dto.TransactionCreateDTO;
import org.xpenbox.transaction.dto.TransactionResponseDTO;
import org.xpenbox.transaction.dto.TransactionUpdateDTO;
import org.xpenbox.transaction.entity.Transaction;
import org.xpenbox.user.entity.User;

import jakarta.inject.Singleton;

/**
 * Mapper class for converting between Transaction entities and DTOs.
 */
@Singleton
public class TransactionMapper implements GenericMapper<Transaction, TransactionCreateDTO, TransactionUpdateDTO, TransactionResponseDTO> {
    private static final Logger LOG = Logger.getLogger(TransactionMapper.class);

    private final CategoryMapper categoryMapper;
    private final IncomeMapper incomeMapper;
    private final AccountMapper accountMapper;
    private final CreditCardMapper creditCardMapper;

    public TransactionMapper(CategoryMapper categoryMapper,
                             IncomeMapper incomeMapper,
                             AccountMapper accountMapper,
                             CreditCardMapper creditCardMapper) {
        this.categoryMapper = categoryMapper;
        this.incomeMapper = incomeMapper;
        this.accountMapper = accountMapper;
        this.creditCardMapper = creditCardMapper;
    }

    /**
     * Maps Transaction entity to TransactionResponseDTO.
     * @param entity The Transaction entity to be mapped.
     * @return The corresponding TransactionResponseDTO.
     */
    @Override
    public TransactionResponseDTO toDTO(Transaction entity) {
        LOG.infof("Mapping Transaction entity to TransactionResponseDTO: %s", entity);
        TransactionResponseDTO dto = new TransactionResponseDTO(
            entity.getResourceCode(),
            entity.getDescription(),
            entity.getTransactionType(),
            entity.getAmount(),
            entity.getLatitude(),
            entity.getLongitude(),
            entity.getTransactionDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            entity.getCategory() != null ? categoryMapper.toSimpleDTO(entity.getCategory()) : null,
            entity.getIncome() != null ? incomeMapper.toSimpleDTO(entity.getIncome()) : null,
            entity.getAccount() != null ? accountMapper.toSimpleDTO(entity.getAccount()) : null,
            entity.getCreditCard() != null ? creditCardMapper.toSimpleDTO(entity.getCreditCard()) : null,
            entity.getDestinationAccount() != null ? accountMapper.toSimpleDTO(entity.getDestinationAccount()) : null
        );
        return dto;
    }

    /**
     * Maps Transaction entity to simple TransactionResponseDTO with limited fields.
     * @param entity The Transaction entity to be mapped.
     * @return The corresponding simple TransactionResponseDTO.
     */
    @Override
    public TransactionResponseDTO toSimpleDTO(Transaction entity) {
        return toDTO(entity);
    }

    /**
     * Maps a list of Transaction entities to a list of TransactionResponseDTOs.
     * @param entities The list of Transaction entities to be mapped.
     * @return The corresponding list of TransactionResponseDTOs.
     */
    @Override
    public List<TransactionResponseDTO> toDTOList(List<Transaction> entities) {
        LOG.infof("Mapping list of Transaction entities to list of TransactionResponseDTOs: %s", entities);

        if (entities == null || entities.isEmpty()) {
            LOG.info("Input entity list is null or empty, returning empty DTO list.");
            return List.of();
        }

        return entities.stream()
                       .map(this::toDTO)
                       .toList();
    }

    /**
     * Maps TransactionCreateDTO to Transaction entity.
     * @param dto The TransactionCreateDTO to be mapped.
     * @return The corresponding Transaction entity.
     */
    @Override
    public Transaction toEntity(TransactionCreateDTO dto, User user) {
        LOG.infof("Mapping TransactionCreateDTO to Transaction entity: %s", dto);
        Transaction entity = new Transaction();
        entity.setResourceCode(ResourceCode.generateTransactionResourceCode());
        entity.setTransactionType(dto.transactionType());
        entity.setDescription(dto.description());
        entity.setAmount(dto.amount());
        entity.setLatitude(dto.latitude());
        entity.setLongitude(dto.longitude());
        entity.setTransactionDate(
            Instant.ofEpochMilli(dto.transactionDateTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
        entity.setUser(user);
        return entity;
    }

    /**
     * Updates an existing Transaction entity with data from TransactionUpdateDTO.
     * @param updateDto The TransactionUpdateDTO containing updated data.
     * @param entity The existing Transaction entity to be updated.
     * @return true if the entity was updated, false otherwise.
     */
    @Override
    public boolean updateEntity(TransactionUpdateDTO updateDto, Transaction entity) {
        LOG.infof("Mapping TransactionUpdateDTO to Transaction Entity: %s", updateDto);
        boolean isUpdated = false;

        if (updateDto.description() != null && !updateDto.description().equals(entity.getDescription())) {
            entity.setDescription(updateDto.description());
            isUpdated = true;
        }

        Long entityTransactionDateTimestamp = entity.getTransactionDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        if (updateDto.transactionDateTimestamp() != null && !updateDto.transactionDateTimestamp().equals(entityTransactionDateTimestamp)) {
            entity.setTransactionDate(
                Instant.ofEpochMilli(updateDto.transactionDateTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime()
            );
            isUpdated = true;
        }

        return isUpdated;
    }
}

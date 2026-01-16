package org.xpenbox.account.mapper;

import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.account.dto.AccountCreateDTO;
import org.xpenbox.account.dto.AccountResponseDTO;
import org.xpenbox.account.dto.AccountUpdateDTO;
import org.xpenbox.account.entity.Account;

/**
 * Mapper class for converting between Account entities and DTOs.
 */
public class AccountMapper {
    private static final Logger LOG = Logger.getLogger(AccountMapper.class);

    /**
     * Maps Account entity to AccountResponseDTO.
     * @param entity The Account entity to be mapped.
     * @return The corresponding AccountResponseDTO.
     */
    public static AccountResponseDTO toDTO(Account entity) {
        LOG.infof("Mapping Account entity to AccountResponseDTO: %s", entity);
        AccountResponseDTO dto = new AccountResponseDTO(
            entity.getResourceCode(),
            entity.getName(),
            entity.getBalance(),
            entity.getClosingDate()
        );
        return dto;
    }

    public static List<AccountResponseDTO> toDTOList(List<Account> entities) {
        LOG.infof("Mapping list of Account entities to list of AccountResponseDTOs");

        if (entities == null || entities.isEmpty()) {
            LOG.infof("No Account entities to map, returning empty list");
            return List.of();
        }

        return entities.stream()
            .map(AccountMapper::toDTO)
            .toList();
    }

    /**
     * Maps AccountCreateDTO to Account entity.
     * @param dto The AccountCreateDTO to be mapped.
     * @return The corresponding Account entity.
     */
    public static Account toEntity(AccountCreateDTO dto) {
        LOG.infof("Mapping AccountResponseDTO to Account entity: %s", dto);
        Account entity = new Account();
        entity.setName(dto.name());
        entity.setBalance(dto.balance());
        return entity;
    }

    /**
     * Maps AccountUpdateDTO to existing Account entity.
     * @param dto The AccountUpdateDTO to be mapped.
     * @param entity The existing Account entity to be updated.
     * @return true if the entity was updated, false otherwise.
     */
    public static boolean toUpdateEntity(AccountUpdateDTO dto, Account entity) {
        LOG.infof("Updating Account entity with AccountUpdateDTO: %s", dto);

        boolean isUpdated = false;

        if (dto.name() != null) {
            entity.setName(dto.name());
            isUpdated = true;
        }

        return isUpdated;
    }
}

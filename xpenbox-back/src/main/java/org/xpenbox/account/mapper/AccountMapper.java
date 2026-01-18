package org.xpenbox.account.mapper;

import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.account.dto.AccountCreateDTO;
import org.xpenbox.account.dto.AccountResponseDTO;
import org.xpenbox.account.dto.AccountUpdateDTO;
import org.xpenbox.account.entity.Account;
import org.xpenbox.common.ResourceCode;
import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.user.entity.User;

import jakarta.inject.Singleton;

/**
 * Mapper class for converting between Account entities and DTOs.
 */
@Singleton
public class AccountMapper implements GenericMapper<Account, AccountCreateDTO, AccountUpdateDTO, AccountResponseDTO> {
    private static final Logger LOG = Logger.getLogger(AccountMapper.class);

    /**
     * Maps Account entity to AccountResponseDTO.
     * @param entity The Account entity to be mapped.
     * @return The corresponding AccountResponseDTO.
     */
    @Override
    public AccountResponseDTO toDTO(Account entity) {
        LOG.infof("Mapping Account entity to AccountResponseDTO: %s", entity);
        AccountResponseDTO dto = new AccountResponseDTO(
            entity.getResourceCode(),
            entity.getName(),
            entity.getBalance(),
            entity.getState(),
            entity.getClosingDate()
        );
        return dto;
    }
    
    /**
     * Maps Account entity to simple AccountResponseDTO.
     * @param entity The Account entity to be mapped.
     * @return The corresponding simple AccountResponseDTO.
     */
    @Override
    public AccountResponseDTO toSimpleDTO(Account entity) {
        LOG.infof("Mapping Account entity to simple AccountResponseDTO: %s", entity);
        AccountResponseDTO dto = new AccountResponseDTO(
            entity.getResourceCode(),
            entity.getName(),
            null,
            null,
            null
        );
        return dto;
    }

    /**
     * Maps list of Account entities to list of AccountResponseDTOs.
     * @param entities The list of Account entities to be mapped.
     * @return The corresponding list of AccountResponseDTOs.
     */
    @Override
    public List<AccountResponseDTO> toDTOList(List<Account> entities) {
        LOG.infof("Mapping list of Account entities to list of AccountResponseDTOs");

        if (entities == null || entities.isEmpty()) {
            LOG.infof("No Account entities to map, returning empty list");
            return List.of();
        }

        return entities.stream()
            .map(this::toDTO)
            .toList();
    }

    /**
     * Maps AccountCreateDTO to Account entity.
     * @param dto The AccountCreateDTO to be mapped.
     * @return The corresponding Account entity.
     */
    @Override
    public Account toEntity(AccountCreateDTO dto, User user) {
        LOG.infof("Mapping AccountResponseDTO to Account entity: %s", dto);
        Account entity = new Account();
        entity.setResourceCode(ResourceCode.generateAccountResourceCode());
        entity.setName(dto.name());
        entity.setBalance(dto.balance());
        entity.setInitialBalance(dto.balance());
        entity.setUser(user);
        return entity;
    }

    /**
     * Maps AccountUpdateDTO to existing Account entity.
     * @param dto The AccountUpdateDTO to be mapped.
     * @param entity The existing Account entity to be updated.
     * @return true if the entity was updated, false otherwise.
     */
    @Override
    public boolean updateEntity(AccountUpdateDTO dto, Account entity) {
        LOG.infof("Updating Account entity with AccountUpdateDTO: %s", dto);

        boolean isUpdated = false;

        if (dto.name() != null) {
            entity.setName(dto.name());
            isUpdated = true;
        }

        return isUpdated;
    }
}

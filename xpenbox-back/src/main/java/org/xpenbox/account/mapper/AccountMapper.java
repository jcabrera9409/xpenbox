package org.xpenbox.account.mapper;

import org.jboss.logging.Logger;
import org.xpenbox.account.dto.AccountCreateDTO;
import org.xpenbox.account.dto.AccountResponseDTO;
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
}

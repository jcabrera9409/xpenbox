package org.xpenbox.creditcard.mapper;

import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.common.ResourceCode;
import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.creditcard.dto.CreditCardCreateDTO;
import org.xpenbox.creditcard.dto.CreditCardResponseDTO;
import org.xpenbox.creditcard.dto.CreditCardUpdateDTO;
import org.xpenbox.creditcard.entity.CreditCard;
import org.xpenbox.user.entity.User;

import jakarta.inject.Singleton;

/**
 * Mapper class for converting between CreditCard entities and DTOs.
 */
@Singleton
public class CreditCardMapper implements GenericMapper<CreditCard, CreditCardCreateDTO, CreditCardUpdateDTO, CreditCardResponseDTO> {
    private static final Logger LOG = Logger.getLogger(CreditCardMapper.class);

    /**
     * Maps CreditCard entity to CreditCardResponseDTO.
     * @param entity The CreditCard entity to be mapped.
     * @return The corresponding CreditCardResponseDTO.
     */
    @Override
    public CreditCardResponseDTO toDTO(CreditCard entity) {
        LOG.infof("Mapping CreditCard entity to CreditCardResponseDTO: %s", entity);
        CreditCardResponseDTO dto = new CreditCardResponseDTO(
            entity.getResourceCode(),
            entity.getName(),
            entity.getCreditLimit(),
            entity.getCurrentBalance(),
            entity.getState(),
            entity.getBillingDay(),
            entity.getPaymentDay(),
            entity.getClosingDate()
        );
        return dto;
    }

    /**
     * Maps CreditCard entity to simple CreditCardResponseDTO with limited fields.
     * @param entity The CreditCard entity to be mapped.
     * @return The corresponding simple CreditCardResponseDTO.
     */
    @Override
    public CreditCardResponseDTO toSimpleDTO(CreditCard entity) {
        LOG.infof("Mapping CreditCard entity to simple CreditCardResponseDTO: %s", entity);
        CreditCardResponseDTO dto = new CreditCardResponseDTO(
            entity.getResourceCode(),
            entity.getName(),
            null,
            null,
            null,
            null,
            null,
            null
        );
        return dto;
    }

    /**
     * Maps list of CreditCard entities to list of CreditCardResponseDTOs.
     * @param entities The list of CreditCard entities to be mapped.
     * @return The corresponding list of CreditCardResponseDTOs.
     */
    @Override
    public List<CreditCardResponseDTO> toDTOList(List<CreditCard> entities) {
        LOG.infof("Mapping list of CreditCard entities to list of CreditCardResponseDTOs");

        if (entities == null || entities.isEmpty()) {
            LOG.infof("No CreditCard entities to map, returning empty list");
            return List.of();
        }

        return entities.stream()
            .map(this::toDTO)
            .toList();
    }

    /**
     * Maps CreditCardCreateDTO to CreditCard entity.
     * @param dto The CreditCardCreateDTO to be mapped.
     * @return The corresponding CreditCard entity.
     */
    @Override
    public CreditCard toEntity(CreditCardCreateDTO dto, User user) {
        LOG.infof("Mapping CreditCardCreateDTO to CreditCard entity: %s", dto);
        CreditCard entity = new CreditCard();
        entity.setResourceCode(ResourceCode.generateCreditCardResourceCode());
        entity.setName(dto.name());
        entity.setCreditLimit(dto.creditLimit());
        entity.setCurrentBalance(dto.currentBalance());
        entity.setBillingDay(dto.billingDay());
        entity.setPaymentDay(dto.paymentDay());
        entity.setUser(user);
        return entity;
    }

    /**
     * Updates CreditCard entity with data from CreditCardUpdateDTO.
     * @param updateDto The CreditCardUpdateDTO containing updated data.
     * @param entity The CreditCard entity to be updated.
     * @return true if the entity was updated, false otherwise.
     */
    @Override
    public boolean updateEntity(CreditCardUpdateDTO updateDto, CreditCard entity) {
        LOG.infof("Updating CreditCard entity with CreditCardUpdateDTO: %s", updateDto);
        boolean isUpdated = false;

        if (updateDto.name() != null && !updateDto.name().equals(entity.getName())) {
            entity.setName(updateDto.name());
            isUpdated = true;
        }
        if (updateDto.creditLimit() != null && updateDto.creditLimit().compareTo(entity.getCreditLimit()) != 0) {
            entity.setCreditLimit(updateDto.creditLimit());
            isUpdated = true;
        }
        if (updateDto.billingDay() != null && !updateDto.billingDay().equals(entity.getBillingDay())) {
            entity.setBillingDay(updateDto.billingDay());
            isUpdated = true;
        }
        if (updateDto.paymentDay() != null && !updateDto.paymentDay().equals(entity.getPaymentDay())) {
            entity.setPaymentDay(updateDto.paymentDay());
            isUpdated = true;
        }
        if (updateDto.state() != null && !updateDto.state().equals(entity.getState())) {
            entity.setState(updateDto.state());
            isUpdated = true;
        }

        return isUpdated;
    }
}

package org.xpenbox.creditcard.mapper;

import org.jboss.logging.Logger;
import org.xpenbox.creditcard.dto.CreditCardCreateDTO;
import org.xpenbox.creditcard.dto.CreditCardResponseDTO;
import org.xpenbox.creditcard.entity.CreditCard;

/**
 * Mapper class for converting between CreditCard entities and DTOs.
 */
public class CreditCardMapper {
    private static final Logger LOG = Logger.getLogger(CreditCardMapper.class);

    /**
     * Maps CreditCard entity to CreditCardResponseDTO.
     * @param entity The CreditCard entity to be mapped.
     * @return The corresponding CreditCardResponseDTO.
     */
    public static CreditCardResponseDTO toDTO(CreditCard entity) {
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
     * Maps CreditCardCreateDTO to CreditCard entity.
     * @param dto The CreditCardCreateDTO to be mapped.
     * @return The corresponding CreditCard entity.
     */
    public static CreditCard toEntity(CreditCardCreateDTO dto) {
        LOG.infof("Mapping CreditCardCreateDTO to CreditCard entity: %s", dto);
        CreditCard entity = new CreditCard();
        entity.setName(dto.name());
        entity.setCreditLimit(dto.creditLimit());
        entity.setCurrentBalance(dto.currentBalance());
        entity.setBillingDay(dto.billingDay());
        entity.setPaymentDay(dto.paymentDay());
        return entity;
    }
}

package org.xpenbox.income.mapper;

import org.jboss.logging.Logger;
import org.xpenbox.income.dto.IncomeCreateDTO;
import org.xpenbox.income.dto.IncomeResponseDTO;
import org.xpenbox.income.entity.Income;

/**
 * Mapper class for converting between Income entities and DTOs.
 */
public class IncomeMapper {
    private static final Logger LOG = Logger.getLogger(IncomeMapper.class);

    /**
     * Maps an Income entity to an IncomeResponseDTO.
     * @param entity The Income entity to map.
     * @return The mapped IncomeResponseDTO.
     */
    public static IncomeResponseDTO toDTO(Income entity) {
        LOG.infof("Mapping Income entity to DTO: %s", entity);
        IncomeResponseDTO dto = new IncomeResponseDTO(
            entity.getResourceCode(),
            entity.getConcept(),
            entity.getIncomeDate(),
            entity.getTotalAmount()
        );
        return dto;
    }

    /**
     * Maps an IncomeCreateDTO to an Income entity.
     * @param dto The IncomeCreateDTO to map.
     * @return The mapped Income entity.
     */
    public static Income toEntity(IncomeCreateDTO dto) {
        LOG.infof("Mapping IncomeCreateDTO to entity: %s", dto);
        Income entity = new Income();
        entity.setConcept(dto.concept());
        entity.setIncomeDate(dto.incomeDate());
        entity.setTotalAmount(dto.totalAmount());
        return entity;
    }
}

package org.xpenbox.income.mapper;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.common.ResourceCode;
import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.income.dto.IncomeCreateDTO;
import org.xpenbox.income.dto.IncomeResponseDTO;
import org.xpenbox.income.dto.IncomeUpdateDTO;
import org.xpenbox.income.entity.Income;
import org.xpenbox.user.entity.User;

import jakarta.inject.Singleton;

/**
 * Mapper class for converting between Income entities and DTOs.
 */
@Singleton
public class IncomeMapper implements GenericMapper<Income, IncomeCreateDTO, IncomeUpdateDTO, IncomeResponseDTO> {
    private static final Logger LOG = Logger.getLogger(IncomeMapper.class);

    /**
     * Maps an Income entity to an IncomeResponseDTO.
     * @param entity The Income entity to map.
     * @return The mapped IncomeResponseDTO.
     */
    @Override
    public IncomeResponseDTO toDTO(Income entity) {
        LOG.infof("Mapping Income entity to DTO: %s", entity);
        IncomeResponseDTO dto = new IncomeResponseDTO(
            entity.getResourceCode(),
            entity.getConcept(),
            entity.getIncomeDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            entity.getTotalAmount()
        );
        return dto;
    }

    /**
     * Maps a list of Income entities to a list of IncomeResponseDTOs.
     * @param entities The list of Income entities to map.
     * @return The list of mapped IncomeResponseDTOs.
     */
    @Override
    public List<IncomeResponseDTO> toDTOList(List<Income> entities) {
        LOG.infof("Mapping list of Income entities to DTOs: %s", entities);
        
        if (entities == null || entities.isEmpty()) {
            LOG.infof("No Income entities to map, returning empty list");
            return List.of();
        }

        return entities.stream()
            .map(this::toDTO)
            .toList();
    }

    /**
     * Maps an IncomeCreateDTO to an Income entity.
     * @param dto The IncomeCreateDTO to map.
     * @param user The User associated with the Income.
     * @return The mapped Income entity.
     */
    @Override
    public Income toEntity(IncomeCreateDTO dto, User user) {
        LOG.infof("Mapping IncomeCreateDTO to entity: %s", dto);
        Income entity = new Income();
        entity.setResourceCode(ResourceCode.generateIncomeResourceCode());
        entity.setConcept(dto.concept());
        entity.setIncomeDate(
            Instant.ofEpochMilli(dto.incomeDateTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
        entity.setTotalAmount(dto.totalAmount());
        entity.setUser(user);
        return entity;
    }

    /**
     * Updates an existing Income entity with data from an IncomeUpdateDTO.
     * @param updateDto The IncomeUpdateDTO containing updated data.
     * @param entity The existing Income entity to update.
     * @return True if the update was successful, false otherwise.
     */
    @Override
    public boolean updateEntity(IncomeUpdateDTO updateDto, Income entity) {
        LOG.infof("Updating Income entity with data from IncomeUpdateDTO: %s", updateDto);
        boolean isUpdated = false;

        if (updateDto.concept() != null && !updateDto.concept().equals(entity.getConcept())) {
            entity.setConcept(updateDto.concept());
            isUpdated = true;
        }

        Long entityIncomeDateTimestamp = entity.getIncomeDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        if (updateDto.incomeDateTimestamp() != null && !updateDto.incomeDateTimestamp().equals(entityIncomeDateTimestamp)) {
            entity.setIncomeDate(
                Instant.ofEpochMilli(updateDto.incomeDateTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime()
            );
            isUpdated = true;
        }

        if (updateDto.totalAmount() != null && !updateDto.totalAmount().equals(entity.getTotalAmount())) {
            entity.setTotalAmount(updateDto.totalAmount());
            isUpdated = true;
        }

        return isUpdated;
    }
}

package org.xpenbox.payment.mapper;

import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.payment.dto.PlanResponseDTO;
import org.xpenbox.payment.entity.Plan;
import org.xpenbox.user.entity.User;

import jakarta.inject.Singleton;

/**
 * Mapper class for converting between Plan entities and DTOs.
 */
@Singleton
public class PlanMapper implements GenericMapper<Plan, PlanResponseDTO, PlanResponseDTO, PlanResponseDTO> {
    private static final Logger LOG = Logger.getLogger(PlanMapper.class.getName());

    /**
     * Converts a Plan entity to a PlanResponseDTO.
     * 
     * @param entity the Plan entity to convert
     * @return the corresponding PlanResponseDTO
     */
    @Override
    public PlanResponseDTO toDTO(Plan entity) {
        LOG.infof("Mapping Plan entity to DTO: %s", entity);
        PlanResponseDTO dto = new PlanResponseDTO(
            entity.getResourceCode(),
            entity.getName(),
            entity.getDescription(),
            entity.getPrice(),
            entity.getCurrency(),
            entity.getBillingCycle()
        );
        return dto;
    }

    /**
     * Converts a Plan entity to a simple PlanResponseDTO. In this case, it is the same as toDTO since there are no additional fields to exclude.
     * 
     * @param entity the Plan entity to convert
     * @return the corresponding PlanResponseDTO
     */
    @Override
    public PlanResponseDTO toSimpleDTO(Plan entity) {
        return toDTO(entity);
    }

    /**
     * Converts a list of Plan entities to a list of PlanResponseDTOs.
     * 
     * @param entities the list of Plan entities to convert
     * @return the corresponding list of PlanResponseDTOs
     */
    @Override
    public List<PlanResponseDTO> toDTOList(List<Plan> entities) {
        return entities.stream().map(this::toDTO).toList();
    }

    /**
     * Converts a PlanResponseDTO to a Plan entity. This method is used for creating new Plan entities from DTOs.
     * @param createDto the PlanResponseDTO to convert
     * @param user the User associated with the Plan (not used in this case, but included for consistency with the GenericMapper interface)
     * @return the corresponding Plan entity
     */
    @Override
    public Plan toEntity(PlanResponseDTO createDto, User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toEntity'");
    }

    /**
     * Updates an existing Plan entity with data from a PlanResponseDTO. This method is used for updating existing Plan entities with new data from DTOs.
     * @param updateDto the PlanResponseDTO containing the updated data
     * @param entity the existing Plan entity to update
     * @return true if the entity was updated, false otherwise
     */
    @Override
    public boolean updateEntity(PlanResponseDTO updateDto, Plan entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateEntity'");
    }

    
}

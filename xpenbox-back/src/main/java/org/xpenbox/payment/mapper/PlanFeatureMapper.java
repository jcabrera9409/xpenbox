package org.xpenbox.payment.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.payment.dto.PlanFeatureResponseDTO;
import org.xpenbox.payment.entity.PlanFeature;
import org.xpenbox.payment.enums.FeatureCodeEnum;
import org.xpenbox.user.entity.User;

import jakarta.inject.Singleton;

/**
 * Mapper class for converting between PlanFeature entity and PlanFeatureResponseDTO. This class implements the GenericMapper interface to provide methods for mapping between the entity and DTO.
 */
@Singleton
public class PlanFeatureMapper implements GenericMapper<PlanFeature, PlanFeatureResponseDTO, PlanFeatureResponseDTO, PlanFeatureResponseDTO> {

    /**
     * Converts a PlanFeature entity to a PlanFeatureResponseDTO. This method maps the fields from the entity to the corresponding fields in the DTO.
     * @param entity The PlanFeature entity to be converted.
     * @return A PlanFeatureResponseDTO containing the data from the entity.
     */
    @Override
    public PlanFeatureResponseDTO toDTO(PlanFeature entity) {
        return new PlanFeatureResponseDTO(
            entity.getFeatureCode(),
            entity.getLimitValue(),
            entity.getIsEnabled()
        );
    }

    /**
     * Converts a PlanFeature entity to a simple PlanFeatureResponseDTO. This method can be used when only a subset of the fields from the entity is needed in the DTO. In this case, it simply calls the toDTO method since all fields are included in the response DTO.
     * @param entity The PlanFeature entity to be converted.
     * @return A PlanFeatureResponseDTO containing the data from the entity.
     */
    @Override
    public PlanFeatureResponseDTO toSimpleDTO(PlanFeature entity) {
        return toDTO(entity);
    }

    /**
     * Converts a list of PlanFeature entities to a list of PlanFeatureResponseDTOs. This method iterates over the list of entities and converts each one to a DTO using the toDTO method, returning a list of DTOs.
     * @param entities The list of PlanFeature entities to be converted.
     * @return A list of PlanFeatureResponseDTOs containing the data from the entities.
     */
    @Override
    public List<PlanFeatureResponseDTO> toDTOList(List<PlanFeature> entities) {
        return entities.stream().map(this::toDTO).toList();
    }

    /**
     * Converts a list of PlanFeature entities to a map of FeatureCodeEnum to PlanFeatureResponseDTO. This method iterates over the list of entities and converts each one to a DTO using the toDTO method, returning a map where the keys are the feature codes (converted to FeatureCodeEnum) and the values are the corresponding DTOs.
     * @param entities The list of PlanFeature entities to be converted.
     * @return A map of FeatureCodeEnum to PlanFeatureResponseDTO containing the data from the entities.
     */
    public Map<FeatureCodeEnum, PlanFeatureResponseDTO> toDTOMap(List<PlanFeature> entities) {
        return entities.stream().collect(Collectors.toMap(entity -> fromString(entity.getFeatureCode()), this::toDTO));
    }

    private FeatureCodeEnum fromString(String code) {
        for (FeatureCodeEnum feature : FeatureCodeEnum.values()) {
            if (feature.name().equalsIgnoreCase(code)) {
                return feature;
            }
        }
        throw new IllegalArgumentException("Unknown feature code: " + code);
    }

    @Override
    public PlanFeature toEntity(PlanFeatureResponseDTO createDto, User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toEntity'");
    }

    @Override
    public boolean updateEntity(PlanFeatureResponseDTO updateDto, PlanFeature entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateEntity'");
    }
    
}

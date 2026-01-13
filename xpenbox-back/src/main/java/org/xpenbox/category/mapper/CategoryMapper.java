package org.xpenbox.category.mapper;

import org.jboss.logging.Logger;
import org.xpenbox.category.dto.CategoryCreateDTO;
import org.xpenbox.category.dto.CategoryResponseDTO;
import org.xpenbox.category.entity.Category;

/**
 * Mapper class for converting between Category entities and DTOs.
 */
public class CategoryMapper {
    private static final Logger LOG = Logger.getLogger(CategoryMapper.class);
    
    /**
     * Converts a Category entity to a CategoryResponseDTO.
     *
     * @param entity the Category entity to convert
     * @return the corresponding CategoryResponseDTO
     */
    public static CategoryResponseDTO toDTO(Category entity) {
        LOG.infof("Mapping Category entity to DTO: %s", entity);
        CategoryResponseDTO dto = new CategoryResponseDTO(
            entity.getResourceCode(),
            entity.getName(),
            entity.getColor(),
            entity.getState()
        );
        return dto;
    }

    /**
     * Converts a CategoryCreateDTO to a Category entity.
     *
     * @param dto the CategoryCreateDTO to convert
     * @return the corresponding Category entity
     */
    public static Category toEntity(CategoryCreateDTO dto) {
        LOG.infof("Mapping CategoryCreateDTO to entity: %s", dto);
        Category entity = new Category();
        entity.setName(dto.name());
        entity.setColor(dto.color());
        return entity;
    }
}

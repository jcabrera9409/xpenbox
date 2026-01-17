package org.xpenbox.category.mapper;

import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.category.dto.CategoryCreateDTO;
import org.xpenbox.category.dto.CategoryResponseDTO;
import org.xpenbox.category.dto.CategoryUpdateDTO;
import org.xpenbox.category.entity.Category;
import org.xpenbox.common.ResourceCode;
import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.user.entity.User;

import jakarta.inject.Singleton;

/**
 * Mapper class for converting between Category entities and DTOs.
 */
@Singleton
public class CategoryMapper implements GenericMapper<Category, CategoryCreateDTO, CategoryUpdateDTO, CategoryResponseDTO> {
    private static final Logger LOG = Logger.getLogger(CategoryMapper.class);
    
    /**
     * Converts a Category entity to a CategoryResponseDTO.
     *
     * @param entity the Category entity to convert
     * @return the corresponding CategoryResponseDTO
     */
    @Override
    public CategoryResponseDTO toDTO(Category entity) {
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
     * Converts a list of Category entities to a list of CategoryResponseDTOs.
     * @param entities the list of Category entities to convert
     * @return the corresponding list of CategoryResponseDTOs
     */
    @Override
    public List<CategoryResponseDTO> toDTOList(List<Category> entities) {
        LOG.infof("Mapping list of Category entities to list of CategoryResponseDTOs");

        if (entities == null || entities.isEmpty()) {
            LOG.infof("No Category entities to map, returning empty list");
            return List.of();
        }

        return entities.stream()
            .map(this::toDTO)
            .toList();
    }

    /**
     * Converts a CategoryCreateDTO to a Category entity.
     *
     * @param dto the CategoryCreateDTO to convert
     * @return the corresponding Category entity
     */
    @Override
    public Category toEntity(CategoryCreateDTO dto, User user) {
        LOG.infof("Mapping CategoryCreateDTO to entity: %s", dto);
        Category entity = new Category();
        entity.setResourceCode(ResourceCode.generateCategoryResourceCode());
        entity.setName(dto.name());
        entity.setColor(dto.color());
        entity.setUser(user);
        return entity;
    }

    /**
     * Updates an existing Category entity with data from a CategoryUpdateDTO.
     * @param dto    the CategoryUpdateDTO containing updated data
     * @param entity the existing Category entity to update
     * @return true if the entity was updated, false otherwise
     */
    @Override
    public boolean updateEntity(CategoryUpdateDTO dto, Category entity) {
        LOG.infof("Updating Category entity: %s with DTO: %s", entity, dto);
        boolean isUpdated = false;

        if (dto.name() != null && !dto.name().equals(entity.getName())) {
            entity.setName(dto.name());
            isUpdated = true;
        }

        if (dto.color() != null && !dto.color().equals(entity.getColor())) {
            entity.setColor(dto.color());
            isUpdated = true;
        }

        if (dto.state() != null && !dto.state().equals(entity.getState())) {
            entity.setState(dto.state());
            isUpdated = true;
        }

        return isUpdated;
    }
}

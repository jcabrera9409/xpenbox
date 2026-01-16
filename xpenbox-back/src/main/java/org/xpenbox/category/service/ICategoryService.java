package org.xpenbox.category.service;

import org.xpenbox.category.dto.CategoryCreateDTO;
import org.xpenbox.category.dto.CategoryResponseDTO;
import org.xpenbox.category.dto.CategoryUpdateDTO;
import org.xpenbox.category.entity.Category;
import org.xpenbox.common.service.IGenericService;

/**
 * Service interface for Category entity operations.
 */
public interface ICategoryService extends IGenericService<Category, CategoryCreateDTO, CategoryUpdateDTO, CategoryResponseDTO> {
    
}

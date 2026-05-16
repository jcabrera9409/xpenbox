package org.xpenbox.category.service;

import java.util.List;

import org.xpenbox.category.dto.CategoryBudgetUsageDTO;
import org.xpenbox.category.dto.CategoryCreateDTO;
import org.xpenbox.category.dto.CategoryResponseDTO;
import org.xpenbox.category.dto.CategoryUpdateDTO;
import org.xpenbox.category.entity.Category;
import org.xpenbox.common.service.IGenericService;

/**
 * Service interface for Category entity operations.
 */
public interface ICategoryService extends IGenericService<Category, CategoryCreateDTO, CategoryUpdateDTO, CategoryResponseDTO> {
    
    /**
     * Retrieves a list of categories along with their usage count and budget used for a specific user.
     * @param userEmail the email of the user for whom to retrieve the category usage information
     * @return a list of CategoryBudgetUsageDTO containing category details, usage count, and budget used
     */
    List<CategoryBudgetUsageDTO> getCategoryBudgetUsageForUser(String userEmail);
}

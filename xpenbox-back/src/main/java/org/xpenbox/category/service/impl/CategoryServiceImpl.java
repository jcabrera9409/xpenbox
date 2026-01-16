package org.xpenbox.category.service.impl;

import org.xpenbox.category.dto.CategoryCreateDTO;
import org.xpenbox.category.dto.CategoryResponseDTO;
import org.xpenbox.category.dto.CategoryUpdateDTO;
import org.xpenbox.category.entity.Category;
import org.xpenbox.category.mapper.CategoryMapper;
import org.xpenbox.category.repository.CategoryRepository;
import org.xpenbox.category.service.ICategoryService;
import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Category Service Implementation
 */
@ApplicationScoped
public class CategoryServiceImpl extends GenericServiceImpl<Category, CategoryCreateDTO, CategoryUpdateDTO, CategoryResponseDTO> implements ICategoryService {
    
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(
        UserRepository userRepository,
        CategoryRepository categoryRepository,
        CategoryMapper categoryMapper
    ) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.categoryMapper =  categoryMapper;
    }

    @Override
    protected String getEntityName() {
        return "Category";
    }

    @Override
    protected UserRepository getUserRepository() {
        return userRepository;
    }

    @Override
    protected CategoryRepository getGenericRepository() {
        return categoryRepository;
    }

    @Override
    protected CategoryMapper getGenericMapper() {
        return categoryMapper;
    }
    
}

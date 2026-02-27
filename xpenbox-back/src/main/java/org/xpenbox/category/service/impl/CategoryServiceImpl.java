package org.xpenbox.category.service.impl;

import org.jboss.logging.Logger;
import org.xpenbox.category.dto.CategoryCreateDTO;
import org.xpenbox.category.dto.CategoryResponseDTO;
import org.xpenbox.category.dto.CategoryUpdateDTO;
import org.xpenbox.category.entity.Category;
import org.xpenbox.category.mapper.CategoryMapper;
import org.xpenbox.category.repository.CategoryRepository;
import org.xpenbox.category.service.ICategoryService;
import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.enforcement.dto.SnapshotPlanDTO;
import org.xpenbox.enforcement.service.IPlanSnapshotService;
import org.xpenbox.enforcement.service.IPlanValidatorService;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Category Service Implementation
 */
@ApplicationScoped
public class CategoryServiceImpl extends GenericServiceImpl<Category, CategoryCreateDTO, CategoryUpdateDTO, CategoryResponseDTO> implements ICategoryService {
    private static final Logger LOG = Logger.getLogger(CategoryServiceImpl.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final IPlanValidatorService planValidatorService;
    private final IPlanSnapshotService planSnapshotService;

    public CategoryServiceImpl(
        UserRepository userRepository,
        CategoryRepository categoryRepository,
        CategoryMapper categoryMapper,
        IPlanValidatorService planValidatorService,
        IPlanSnapshotService planSnapshotService
    ) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.categoryMapper =  categoryMapper;
        this.planValidatorService = planValidatorService;
        this.planSnapshotService = planSnapshotService;
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

    @Override
    public CategoryResponseDTO create(CategoryCreateDTO categoryCreateDTO, String userEmail) {
        LOG.infof("Validating plan limits for user email: %s before creating category", userEmail);

        SnapshotPlanDTO activePlanSnapshot = planSnapshotService.getPlanSnapshotByEmail(userEmail);
        planValidatorService.validateCanCreateCategories(activePlanSnapshot);

        return super.create(categoryCreateDTO, userEmail);
    }
    
}

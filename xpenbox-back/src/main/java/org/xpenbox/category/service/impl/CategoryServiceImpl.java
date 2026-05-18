package org.xpenbox.category.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.xpenbox.category.dto.CategoryBudgetUsageDTO;
import org.xpenbox.category.dto.CategoryCreateDTO;
import org.xpenbox.category.dto.CategoryResponseDTO;
import org.xpenbox.category.dto.CategoryUpdateDTO;
import org.xpenbox.category.entity.Category;
import org.xpenbox.category.mapper.CategoryMapper;
import org.xpenbox.category.repository.CategoryRepository;
import org.xpenbox.category.service.ICategoryService;
import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.dashboard.dto.PeriodFilter;
import org.xpenbox.enforcement.dto.SnapshotPlanDTO;
import org.xpenbox.enforcement.service.IPlanSnapshotService;
import org.xpenbox.enforcement.service.IPlanValidatorService;
import org.xpenbox.transaction.entity.Transaction;
import org.xpenbox.transaction.entity.Transaction.TransactionType;
import org.xpenbox.transaction.repository.TransactionRepository;
import org.xpenbox.user.entity.User;
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
    private final TransactionRepository transactionRepository;

    public CategoryServiceImpl(
        UserRepository userRepository,
        CategoryRepository categoryRepository,
        CategoryMapper categoryMapper,
        IPlanValidatorService planValidatorService,
        IPlanSnapshotService planSnapshotService,
        TransactionRepository transactionRepository
    ) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.categoryMapper =  categoryMapper;
        this.planValidatorService = planValidatorService;
        this.planSnapshotService = planSnapshotService;
        this.transactionRepository = transactionRepository;
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

    @Override
    public List<CategoryBudgetUsageDTO> getCategoryBudgetUsageForUser(String userEmail) {
        LOG.infof("Fetching category budget usage for user email: %s", userEmail);
        User user = validateAndGetUser(userEmail);

        Map<String, LocalDateTime> dateRange = PeriodFilter.getDateRange(PeriodFilter.CURRENT_MONTH);
        List<Transaction> transactions = transactionRepository.findByUserIdAndPeriodRange(user.id, dateRange.get("from"), dateRange.get("to"));

        List<Transaction> transactionFilter = transactions.stream()
            .filter(
                tx -> tx.getCategory() != null && (tx.getTransactionType().equals(TransactionType.EXPENSE)
                        || tx.getTransactionType().equals(TransactionType.CREDIT_PAYMENT))
                    && tx.getAccount() != null
            )
            .toList();

        List<CategoryResponseDTO> categories = extractCategoriesFromTransactions(transactionFilter);

        return categories.stream()
            .map(category -> mapToCategoryBudgetUsageDTO(category, transactionFilter))
            .toList();

    }

    private List<CategoryResponseDTO> extractCategoriesFromTransactions(List<Transaction> transactions) {
        return transactions.stream()
            .filter(tx -> tx.getCategory() != null)
            .map(tx -> categoryMapper.toDTO(tx.getCategory()))
            .distinct()
            .toList();
    }

    private CategoryBudgetUsageDTO mapToCategoryBudgetUsageDTO(CategoryResponseDTO category, List<Transaction> transactions) {
        int usageCount = (int) transactions.stream()
            .filter(tx -> tx.getCategory() != null && tx.getCategory().getResourceCode().equals(category.resourceCode()))
            .count();

        BigDecimal budgetUsed = transactions.stream()
            .filter(tx -> tx.getCategory() != null && tx.getCategory().getResourceCode().equals(category.resourceCode()))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CategoryBudgetUsageDTO(category, usageCount, budgetUsed);
    }
}

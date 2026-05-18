package org.xpenbox.category.dto;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for representing category usage and budget information in the dashboard.
 * @param category the category details
 * @param usageCount the number of transactions using this category
 * @param budgetUsed the total amount used from the category's budget
 */
@RegisterForReflection
public record CategoryBudgetUsageDTO(
    CategoryResponseDTO category,
    int usageCount,
    BigDecimal budgetUsed
) { }

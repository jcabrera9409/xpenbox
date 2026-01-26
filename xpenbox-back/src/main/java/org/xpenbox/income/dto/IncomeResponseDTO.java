package org.xpenbox.income.dto;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for responding with Income records.
 * @param resourceCode          Unique code identifying the income resource.
 * @param concept               Description of the income.
 * @param incomeDateTimestamp   Timestamp when the income was recorded.
 * @param totalAmount           Total amount of the income.
 * @param allocatedAmount       Amount of the income that has been allocated.
 */
@RegisterForReflection
public record IncomeResponseDTO (
    String resourceCode,
    String concept,
    Long incomeDateTimestamp,
    BigDecimal totalAmount,
    BigDecimal allocatedAmount
) { }

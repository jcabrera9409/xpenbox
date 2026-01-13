package org.xpenbox.income.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for responding with Income records.
 * @param resourceCode Unique code identifying the income resource.
 * @param concept      Description of the income.
 * @param incomeDate   Date and time when the income was recorded.
 * @param totalAmount  Total amount of the income.
 */
@RegisterForReflection
public record IncomeResponseDTO (
    String resourceCode,
    String concept,
    LocalDateTime incomeDate,
    BigDecimal totalAmount
) { }

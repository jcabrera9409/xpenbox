package org.xpenbox.income.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for updating existing Income records.
 * @param concept      Description of the income.
 * @param incomeDate   Date and time when the income was recorded.
 * @param totalAmount  Total amount of the income.
 */
@RegisterForReflection
public record IncomeUpdateDTO (
    @Size(max = 150, message = "Concept must not exceed 150 characters")
    String concept,

    LocalDateTime incomeDate,

    @Min(value = 1, message = "Total amount must be at least 1")
    BigDecimal totalAmount
) { }

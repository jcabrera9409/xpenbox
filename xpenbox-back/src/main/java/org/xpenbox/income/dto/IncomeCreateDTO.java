package org.xpenbox.income.dto;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating new Income records.
 * @param concept      Description of the income.
 * @param incomeDateTimestamp   Timestamp when the income was recorded.
 * @param totalAmount  Total amount of the income.
 */
@RegisterForReflection
public record IncomeCreateDTO (

    @NotNull(message = "Concept must not be null")
    @Size(max = 150, message = "Concept must not exceed 150 characters")
    String concept,

    @NotNull(message = "Income date must not be null")
    @Min(value = 1, message = "Income date must be a valid timestamp")
    Long incomeDateTimestamp,

    @NotNull(message = "Total amount must not be null")
    @Min(value = 1, message = "Total amount must be at least 1")
    BigDecimal totalAmount

) { }

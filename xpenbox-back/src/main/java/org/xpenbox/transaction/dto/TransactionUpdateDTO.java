package org.xpenbox.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for updating an existing Transaction.
 * @param description Description of the transaction.
 * @param amount Amount involved in the transaction.
 * @param transactionDate Date and time of the transaction.
 * @param categoryResourceCode Resource code of the associated category.
 * @param accountResourceCode Resource code of the associated account.
 * @param creditCardResourceCode Resource code of the associated credit card.
 */
@RegisterForReflection
public record TransactionUpdateDTO (
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than zero")
    BigDecimal amount,

    LocalDateTime transactionDate,

    @Size(min = 1, max = 50, message = "Category resource code must be between 1 and 50 characters")
    String categoryResourceCode,

    @Size(min = 1, max = 50, message = "Account resource code must be between 1 and 50 characters")
    String accountResourceCode,

    @Size(min = 1, max = 50, message = "Credit card resource code must be between 1 and 50 characters")
    String creditCardResourceCode
) { }

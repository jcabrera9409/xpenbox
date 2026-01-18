package org.xpenbox.transaction.dto;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for updating an existing Transaction.
 * @param description Description of the transaction.
 * @param amount Amount involved in the transaction.
 * @param transactionDateTimestamp Timestamp of the transaction date and time.
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

    @Min(value = 1, message = "Transaction date timestamp must be a positive value")
    Long transactionDateTimestamp,

    @Size(min = 1, max = 50, message = "Category resource code must be between 1 and 50 characters")
    String categoryResourceCode,

    @Size(min = 1, max = 50, message = "Account resource code must be between 1 and 50 characters")
    String accountResourceCode,

    @Size(min = 1, max = 50, message = "Credit card resource code must be between 1 and 50 characters")
    String creditCardResourceCode
) { }

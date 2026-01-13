package org.xpenbox.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating a new Transaction.
 * @param description Description of the transaction.
 * @param amount Amount involved in the transaction.
 * @param latitude Latitude where the transaction took place.
 * @param longitude Longitude where the transaction took place.
 * @param transactionDate Date and time of the transaction.
 * @param categoryResourceCode Resource code of the associated category.
 * @param incomeResourceCode Resource code of the associated income.
 * @param accountResourceCode Resource code of the associated account.
 * @param creditCardResourceCode Resource code of the associated credit card.
 * @param destinationAccountResourceCode Resource code of the destination account (for transfers).
 */
@RegisterForReflection
public record TransactionCreateDTO (

    @NotNull(message = "Description cannot be null")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than zero")
    BigDecimal amount,

    @DecimalMin(value = "-90.00000000", inclusive = true, message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.00000000", inclusive = true, message = "Latitude must be between -90 and 90")
    BigDecimal latitude,

    @DecimalMin(value = "-180.00000000", inclusive = true, message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.00000000", inclusive = true, message = "Longitude must be between -180 and 180")
    BigDecimal longitude,

    @NotNull(message = "Transaction date cannot be null")
    LocalDateTime transactionDate,

    @Size(min = 1, max = 50, message = "Category resource code must be between 1 and 50 characters")
    String categoryResourceCode,

    @Size(min = 1, max = 50, message = "Income resource code must be between 1 and 50 characters")
    String incomeResourceCode,

    @Size(min = 1, max = 50, message = "Account resource code must be between 1 and 50 characters")
    String accountResourceCode,

    @Size(min = 1, max = 50, message = "Credit card resource code must be between 1 and 50 characters")
    String creditCardResourceCode,

    @Size(min = 1, max = 50, message = "Destination account resource code must be between 1 and 50 characters")
    String destinationAccountResourceCode
) { }

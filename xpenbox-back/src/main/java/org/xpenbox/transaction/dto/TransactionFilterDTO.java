package org.xpenbox.transaction.dto;

import org.xpenbox.transaction.entity.Transaction.TransactionType;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for filtering transactions.
 * @param resourceCode Resource code of the transaction
 * @param transactionType Type of the transaction
 * @param description Description of the transaction
 * @param transactionDateTimestampFrom Start timestamp for transaction date filter
 * @param transactionDateTimestampTo End timestamp for transaction date filter
 * @param categoryResourceCode Resource code of the category
 * @param incomeResourceCode Resource code of the income
 * @param accountResourceCode Resource code of the account
 * @param creditCardResourceCode Resource code of the credit card
 * @param pageNumber Page number for pagination
 * @param pageSize Page size for pagination
 */
@RegisterForReflection
public record TransactionFilterDTO(
    @Size(max = 100, message = "Resource code must not exceed 100 characters")
    String resourceCode,

    TransactionType transactionType,
   
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    @Min(value = 1, message = "Date from timestamp must be a positive number")
    Long transactionDateTimestampFrom,

    @Min(value = 1, message = "Date to timestamp must be a positive number")
    Long transactionDateTimestampTo,

    @Size(max = 100, message = "Category resource code must not exceed 100 characters")
    String categoryResourceCode,

    @Size(max = 100, message = "Income resource code must not exceed 100 characters")
    String incomeResourceCode,

    @Size(max = 100, message = "Account resource code must not exceed 100 characters")
    String accountResourceCode,

    @Size(max = 100, message = "Credit card resource code must not exceed 100 characters")
    String creditCardResourceCode,

    @Min(value = 0, message = "Page number must be a non-negative number")
    Integer pageNumber,
    
    @Min(value = 1, message = "Page size must be a positive number")
    Integer pageSize
) {}

package org.xpenbox.transaction.dto;

import org.xpenbox.transaction.entity.Transaction.TransactionType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

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

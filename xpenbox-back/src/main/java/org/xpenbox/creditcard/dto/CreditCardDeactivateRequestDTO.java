package org.xpenbox.creditcard.dto;

import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for deactivating a Credit Card.
 * @param accountResourceCode Resource code of the account to pay off the credit card balance.
 */
public record CreditCardDeactivateRequestDTO(

    @Size(min = 1, max = 100, message = "Account resource code for transfer to must be between 1 and 100 characters")
    String accountResourceCode
) { }

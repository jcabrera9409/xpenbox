package org.xpenbox.creditcard.service;

import java.math.BigDecimal;

import org.xpenbox.common.service.IGenericService;
import org.xpenbox.creditcard.dto.CreditCardCreateDTO;
import org.xpenbox.creditcard.dto.CreditCardDeactivateRequestDTO;
import org.xpenbox.creditcard.dto.CreditCardResponseDTO;
import org.xpenbox.creditcard.dto.CreditCardUpdateDTO;
import org.xpenbox.creditcard.entity.CreditCard;

/**
 * Service interface for CreditCard entity operations.
 */
public interface ICreditCardService extends IGenericService<CreditCard, CreditCardCreateDTO, CreditCardUpdateDTO, CreditCardResponseDTO> {
    
    /**
     * Process adding amount to the credit card balance.
     * @param id the credit card id
     * @param amount the amount to add
     */
    public void processAddAmount(Long id, BigDecimal amount);

    /**
     * Process adding payment to the credit card.
     * @param resourceCode the credit card resource code
     * @param userId the user id
     * @param amount the payment amount
     */
    public void processAddPayment(String resourceCode, Long userId, BigDecimal amount);

    /**
     * Deactivate a credit card and pay off its balance using a specified account.
     * @param resourceCode the resource code of the credit card to deactivate
     * @param creditCardDeactivateRequestDTO the DTO containing the account resource code for payment
     * @param userEmail the email of the user performing the operation
     */
    public void deactivateCreditCard(String resourceCode, CreditCardDeactivateRequestDTO creditCardDeactivateRequestDTO, String userEmail);
}

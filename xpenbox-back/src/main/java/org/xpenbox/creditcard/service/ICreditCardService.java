package org.xpenbox.creditcard.service;

import org.xpenbox.common.service.IGenericService;
import org.xpenbox.creditcard.dto.CreditCardCreateDTO;
import org.xpenbox.creditcard.dto.CreditCardResponseDTO;
import org.xpenbox.creditcard.dto.CreditCardUpdateDTO;
import org.xpenbox.creditcard.entity.CreditCard;

/**
 * Service interface for CreditCard entity operations.
 */
public interface ICreditCardService extends IGenericService<CreditCard, CreditCardCreateDTO, CreditCardUpdateDTO, CreditCardResponseDTO> {
    
}

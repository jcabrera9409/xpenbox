package org.xpenbox.creditcard.service.impl;

import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.creditcard.dto.CreditCardCreateDTO;
import org.xpenbox.creditcard.dto.CreditCardResponseDTO;
import org.xpenbox.creditcard.dto.CreditCardUpdateDTO;
import org.xpenbox.creditcard.entity.CreditCard;
import org.xpenbox.creditcard.mapper.CreditCardMapper;
import org.xpenbox.creditcard.repository.CreditCardRepository;
import org.xpenbox.creditcard.service.ICreditCardService;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * CreditCardServiceImpl provides the implementation of credit card related operations.
 */
@ApplicationScoped
public class CreditCardServiceImpl extends GenericServiceImpl<CreditCard, CreditCardCreateDTO, CreditCardUpdateDTO, CreditCardResponseDTO> implements ICreditCardService {

    private final UserRepository userRepository;
    private final CreditCardRepository creditCardRepository;
    private final CreditCardMapper creditCardMapper;

    public CreditCardServiceImpl(
        UserRepository userRepository,
        CreditCardRepository creditCardRepository,
        CreditCardMapper creditCardMapper
    ) {
        this.userRepository = userRepository;
        this.creditCardRepository = creditCardRepository;
        this.creditCardMapper = creditCardMapper;
    }

    @Override
    protected String getEntityName() {
        return "CreditCard";
    }

    @Override
    protected UserRepository getUserRepository() {
        return userRepository;
    }

    @Override
    protected CreditCardRepository getGenericRepository() {
        return creditCardRepository;
    }

    @Override
    protected GenericMapper<CreditCard, CreditCardCreateDTO, CreditCardUpdateDTO, CreditCardResponseDTO> getGenericMapper() {
        return creditCardMapper;
    }
    
}

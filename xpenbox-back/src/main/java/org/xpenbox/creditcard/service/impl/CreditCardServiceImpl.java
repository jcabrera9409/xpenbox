package org.xpenbox.creditcard.service.impl;

import java.math.BigDecimal;

import org.jboss.logging.Logger;
import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.creditcard.dto.CreditCardCreateDTO;
import org.xpenbox.creditcard.dto.CreditCardResponseDTO;
import org.xpenbox.creditcard.dto.CreditCardUpdateDTO;
import org.xpenbox.creditcard.entity.CreditCard;
import org.xpenbox.creditcard.mapper.CreditCardMapper;
import org.xpenbox.creditcard.repository.CreditCardRepository;
import org.xpenbox.creditcard.service.ICreditCardService;
import org.xpenbox.exception.InsufficientFoundsException;
import org.xpenbox.exception.ResourceNotFoundException;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * CreditCardServiceImpl provides the implementation of credit card related operations.
 */
@ApplicationScoped
public class CreditCardServiceImpl extends GenericServiceImpl<CreditCard, CreditCardCreateDTO, CreditCardUpdateDTO, CreditCardResponseDTO> implements ICreditCardService {
    private static final Logger LOG = Logger.getLogger(CreditCardServiceImpl.class);

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

    @Override
    public void processAddAmount(Long id, BigDecimal amount) {
        LOG.infof("Processing add amount for CreditCard ID: %d with amount: %s", id, amount);

        CreditCard creditCard = creditCardRepository.findByIdOptional(id)
            .orElseThrow(() -> {
                return new ResourceNotFoundException("CreditCard not found");
            });

        if (creditCard.getCreditLimit().compareTo(creditCard.getCurrentBalance().add(amount)) < 0) {
            LOG.debugf("Credit limit exceeded for CreditCard ID %d: Current balance %s, Requested amount %s, Credit limit %s", id, creditCard.getCurrentBalance(), amount, creditCard.getCreditLimit());
            throw new InsufficientFoundsException("Credit limit exceeded");
        }

        creditCard.setCurrentBalance(creditCard.getCurrentBalance().add(amount));

        creditCardRepository.persist(creditCard);
        LOG.infof("Amount added successfully for CreditCard ID: %d. New balance: %s", id, creditCard.getCurrentBalance());
    }
    
}

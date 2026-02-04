package org.xpenbox.creditcard.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.creditcard.dto.CreditCardCreateDTO;
import org.xpenbox.creditcard.dto.CreditCardDeactivateRequestDTO;
import org.xpenbox.creditcard.dto.CreditCardResponseDTO;
import org.xpenbox.creditcard.dto.CreditCardUpdateDTO;
import org.xpenbox.creditcard.entity.CreditCard;
import org.xpenbox.creditcard.mapper.CreditCardMapper;
import org.xpenbox.creditcard.repository.CreditCardRepository;
import org.xpenbox.creditcard.service.ICreditCardService;
import org.xpenbox.exception.BadRequestException;
import org.xpenbox.exception.InsufficientFoundsException;
import org.xpenbox.exception.ResourceNotFoundException;
import org.xpenbox.transaction.dto.TransactionCreateDTO;
import org.xpenbox.transaction.entity.Transaction.TransactionType;
import org.xpenbox.transaction.service.ITransactionService;
import org.xpenbox.user.entity.User;
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
    private final ITransactionService transactionService;

    public CreditCardServiceImpl(
        UserRepository userRepository,
        CreditCardRepository creditCardRepository,
        CreditCardMapper creditCardMapper,
        ITransactionService transactionService
    ) {
        this.userRepository = userRepository;
        this.creditCardRepository = creditCardRepository;
        this.creditCardMapper = creditCardMapper;
        this.transactionService = transactionService;
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
    public List<CreditCardResponseDTO> getAll(String userEmail) {
        return super.getAll(userEmail).stream()
            .filter(CreditCardResponseDTO::state)
            .toList();
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

    @Override
    public void processAddPayment(String resourceCode, Long userId, BigDecimal amount) {
        LOG.infof("Processing add payment for CreditCard resource code: %s with amount: %s", resourceCode, amount);
        CreditCard creditCard = validateAndGetCreditCard(resourceCode, userId);
        
        creditCard.setCurrentBalance(creditCard.getCurrentBalance().subtract(amount));

        creditCardRepository.persist(creditCard);
        LOG.infof("Payment added successfully for CreditCard resource code: %s. New balance: %s", resourceCode, creditCard.getCurrentBalance());
    }

    @Override
    public void deactivateCreditCard(String resourceCode, CreditCardDeactivateRequestDTO creditCardDeactivateRequestDTO, String userEmail) {
        LOG.infof("Deactivating CreditCard with resource code: %s for user: %s", resourceCode, userEmail);
        
        User user = super.validateAndGetUser(userEmail);
        
        CreditCard creditCard = validateAndGetCreditCard(resourceCode, user.id);

        if (creditCard.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
            
            TransactionCreateDTO transactionCreateDTO = new TransactionCreateDTO(
                TransactionType.CREDIT_PAYMENT,
                "Auto-generated payment on CreditCard deactivation: " + creditCard.getName(),
                creditCard.getCurrentBalance(), 
                null, 
                null, 
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), 
                null, 
                null,
                creditCardDeactivateRequestDTO.accountResourceCode(),
                creditCard.getResourceCode(),
                null
            );

            transactionService.create(transactionCreateDTO, userEmail);
        }

        creditCard.setClosingDate(LocalDateTime.now());
        creditCard.setState(false);

        creditCardRepository.persist(creditCard);
        LOG.infof("CreditCard with resource code: %s deactivated successfully", resourceCode);
    }

    private CreditCard validateAndGetCreditCard(String resourceCode, Long userId) {
        if (resourceCode == null || resourceCode.isEmpty()) {
            LOG.debug("Resource code is null or empty");
            throw new BadRequestException("CreditCard resource code must be provided");
        }

        CreditCard creditCard = creditCardRepository.findByResourceCodeAndUserId(resourceCode, userId)
            .orElseThrow(() -> {
                LOG.debugf("CreditCard with resource code %s not found for user ID %d", resourceCode, userId);
                return new BadRequestException("CreditCard to deactivate not found");
            });

        if (!creditCard.getState()) {
            LOG.debugf("CreditCard with resource code %s is already deactivated", resourceCode);
            throw new BadRequestException("CreditCard is already deactivated");
        }
        return creditCard;
    }
}

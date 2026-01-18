package org.xpenbox.transaction.repository;

import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.transaction.entity.Transaction;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransactionRepository extends GenericRepository<Transaction> {
    
}

package org.xpenbox.income.repository;

import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.income.entity.Income;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for Income entity operations.
 */
@ApplicationScoped
public class IncomeRepository extends GenericRepository<Income> {
    
}

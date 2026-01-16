package org.xpenbox.account.repository;

import org.xpenbox.account.entity.Account;
import org.xpenbox.common.repository.GenericRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for Account entity.
 */
@ApplicationScoped
public class AccountRepository extends GenericRepository<Account> {
}

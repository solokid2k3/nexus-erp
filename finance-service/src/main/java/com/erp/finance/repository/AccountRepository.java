package com.erp.finance.repository;

import com.erp.finance.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByAccountTypeAndIsActiveTrue(Account.AccountType type);

    List<Account> findByIsActiveTrue();

    @Query("SELECT a FROM Account a WHERE a.isActive = true AND " +
           "(a.name LIKE %:search% OR a.accountNumber LIKE %:search%)")
    List<Account> searchAccounts(String search);

    @Query("SELECT a FROM Account a WHERE a.accountType IN ('REVENUE', 'EXPENSE') AND a.isActive = true")
    List<Account> findIncomeStatementAccounts();

    @Query("SELECT a FROM Account a WHERE a.accountType IN ('ASSET', 'LIABILITY', 'EQUITY') AND a.isActive = true")
    List<Account> findBalanceSheetAccounts();
}

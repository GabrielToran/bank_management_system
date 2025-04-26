package com.bankapp.service;

import com.bankapp.model.entity.AccountEntity;
import com.bankapp.model.entity.TransactionEntity;
import com.bankapp.model.exception.AccountNotFoundException;
import com.bankapp.model.exception.BankOperationException;
import com.bankapp.model.exception.InsufficientFundsException;
import com.bankapp.model.repository.AccountRepository;
import com.bankapp.model.repository.TransactionRepository;
import com.bankapp.util.AuditLogger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EntityManager entityManager;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository, 
                         EntityManager entityManager) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.entityManager = entityManager;
    }

    public AccountEntity createAccount(AccountEntity account, String username) {
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            
            validateAmount(account.getBalance(), "Initial balance");
            account.updateAuditInfo(username);
            account = accountRepository.save(account);

            // Record initial deposit transaction if balance > 0
            if (account.getBalance() > 0) {
                TransactionEntity transaction = new TransactionEntity();
                transaction.setAccount(account);
                transaction.setAmount(account.getBalance());
                transaction.setType("DEPOSIT");
                transaction.setDescription("Initial deposit");
                transaction.updateAuditInfo(username);
                transactionRepository.save(transaction);
            }

            tx.commit();
            AuditLogger.log(username, "Created account: " + account.getAccountId());
            return account;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public AccountEntity getAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));
    }

    public List<AccountEntity> getAccountsByCustomer(String customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAll();
    }

    public AccountEntity deposit(String accountId, double amount, String description, String username) {
        validateAmount(amount, "Deposit amount");

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            
            AccountEntity account = getAccount(accountId);
            account.setBalance(account.getBalance() + amount);
            account.updateAuditInfo(username);
            account = accountRepository.save(account);

            TransactionEntity transaction = new TransactionEntity();
            transaction.setAccount(account);
            transaction.setAmount(amount);
            transaction.setType("DEPOSIT");
            transaction.setDescription(description);
            transaction.updateAuditInfo(username);
            transactionRepository.save(transaction);

            tx.commit();
            AuditLogger.log(username, "Deposited " + amount + " to account: " + accountId);
            return account;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new BankOperationException("Deposit failed: " + e.getMessage(), e);
        }
    }

    public AccountEntity withdraw(String accountId, double amount, String description, String username) {
        validateAmount(amount, "Withdrawal amount");

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            
            AccountEntity account = getAccount(accountId);
            
            if (account.getBalance() < amount) {
                throw new InsufficientFundsException("Insufficient funds for withdrawal");
            }

            account.setBalance(account.getBalance() - amount);
            account.updateAuditInfo(username);
            account = accountRepository.save(account);

            TransactionEntity transaction = new TransactionEntity();
            transaction.setAccount(account);
            transaction.setAmount(amount);
            transaction.setType("WITHDRAWAL");
            transaction.setDescription(description);
            transaction.updateAuditInfo(username);
            transactionRepository.save(transaction);

            tx.commit();
            AuditLogger.log(username, "Withdrew " + amount + " from account: " + accountId);
            return account;
        } catch (InsufficientFundsException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new BankOperationException("Withdrawal failed: " + e.getMessage(), e);
        }
    }

    public AccountEntity transfer(String fromAccountId, String toAccountId, double amount, String description, String username) {
        validateAmount(amount, "Transfer amount");

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            
            AccountEntity fromAccount = getAccount(fromAccountId);
            AccountEntity toAccount = getAccount(toAccountId);

            if (fromAccount.getBalance() < amount) {
                throw new InsufficientFundsException("Insufficient funds for transfer");
            }

            // Update source account
            fromAccount.setBalance(fromAccount.getBalance() - amount);
            fromAccount.updateAuditInfo(username);
            fromAccount = accountRepository.save(fromAccount);

            // Update target account
            toAccount.setBalance(toAccount.getBalance() + amount);
            toAccount.updateAuditInfo(username);
            toAccount = accountRepository.save(toAccount);

            // Record transfer transaction
            TransactionEntity transaction = new TransactionEntity();
            transaction.setAccount(fromAccount);
            transaction.setAmount(amount);
            transaction.setType("TRANSFER");
            transaction.setDescription(description + " (To: " + toAccountId + ")");
            transaction.updateAuditInfo(username);
            transactionRepository.save(transaction);

            tx.commit();
            AuditLogger.log(username, "Transferred " + amount + " from account " + fromAccountId + " to " + toAccountId);
            return fromAccount;
        } catch (InsufficientFundsException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new BankOperationException("Transfer failed: " + e.getMessage(), e);
        }
    }

    public List<TransactionEntity> getTransactions(String accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    public boolean closeAccount(String accountId, String username) {
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            
            AccountEntity account = getAccount(accountId);
            
            if (account.getBalance() != 0) {
                throw new BankOperationException("Cannot close account with non-zero balance");
            }

            account.setStatus("CLOSED");
            account.updateAuditInfo(username);
            accountRepository.save(account);

            tx.commit();
            AuditLogger.log(username, "Closed account: " + accountId);
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new BankOperationException("Failed to close account: " + e.getMessage(), e);
        }
    }

    private void validateAmount(double amount, String fieldName) {
        if (amount < 0) {
            throw new BankOperationException(fieldName + " cannot be negative");
        }
    }
}

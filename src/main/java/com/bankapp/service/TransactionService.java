package com.bankapp.service;

import com.bankapp.model.transaction.Transaction;
import com.bankapp.model.account.Account;
import com.bankapp.model.exception.InsufficientFundsException;
import com.bankapp.model.exception.AccountNotFoundException;
import com.bankapp.model.exception.BankOperationException;
import com.bankapp.model.repository.TransactionRepository;
import com.bankapp.model.repository.AccountRepository;
import com.bankapp.util.AuditLogger;

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

/**
 * Service class that handles transaction-related business logic.
 */
public class TransactionService {

    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private AuditLogger auditLogger;

    /**
     * Constructor for TransactionService.
     *
     * @param transactionRepository The repository for transactions
     * @param accountRepository The repository for accounts
     * @param auditLogger The audit logger for transaction activities
     */
    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              AuditLogger auditLogger) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.auditLogger = auditLogger;
    }

    /**
     * Retrieves transaction history for a specific account.
     *
     * @param accountId The ID of the account
     * @return List of transactions
     * @throws AccountNotFoundException If account is not found
     */
    public List<Transaction> getTransactionHistory(long accountId) throws AccountNotFoundException {
        // Verify account exists
        if (!accountRepository.exists(accountId)) {
            throw new AccountNotFoundException("Account with ID " + accountId + " not found");
        }

        return transactionRepository.findByAccountId(accountId);
    }

    /**
     * Deposits money into an account.
     *
     * @param accountId The ID of the account
     * @param amount The amount to deposit
     * @return The transaction record
     * @throws AccountNotFoundException If account is not found
     * @throws BankOperationException If amount is invalid or operation fails
     */
    public Transaction deposit(long accountId, BigDecimal amount)
            throws AccountNotFoundException, BankOperationException {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankOperationException("Deposit amount must be positive");
        }

        Account account = accountRepository.findById(accountId);
        if (account == null) {
            throw new AccountNotFoundException("Account with ID " + accountId + " not found");
        }

        // Update account balance
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        accountRepository.update(account);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setType("DEPOSIT");
        transaction.setDescription("Deposit to account");
        transaction.setTimestamp(new Date());
        transaction.setBalance(newBalance);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Log the transaction
        auditLogger.logTransaction(transaction);

        return savedTransaction;
    }

    /**
     * Withdraws money from an account.
     *
     * @param accountId The ID of the account
     * @param amount The amount to withdraw
     * @return The transaction record
     * @throws AccountNotFoundException If account is not found
     * @throws InsufficientFundsException If account has insufficient funds
     * @throws BankOperationException If amount is invalid or operation fails
     */
    public Transaction withdraw(long accountId, BigDecimal amount)
            throws AccountNotFoundException, InsufficientFundsException, BankOperationException {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankOperationException("Withdrawal amount must be positive");
        }

        Account account = accountRepository.findById(accountId);
        if (account == null) {
            throw new AccountNotFoundException("Account with ID " + accountId + " not found");
        }

        // Check sufficient funds
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }

        // Update account balance
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accountRepository.update(account);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setAmount(amount.negate()); // Negative amount for withdrawal
        transaction.setType("WITHDRAWAL");
        transaction.setDescription("Withdrawal from account");
        transaction.setTimestamp(new Date());
        transaction.setBalance(newBalance);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Log the transaction
        auditLogger.logTransaction(transaction);

        return savedTransaction;
    }

    /**
     * Transfers money between accounts.
     *
     * @param fromAccountId The ID of the source account
     * @param toAccountId The ID of the destination account
     * @param amount The amount to transfer
     * @return The transaction record for the transfer
     * @throws AccountNotFoundException If either account is not found
     * @throws InsufficientFundsException If source account has insufficient funds
     * @throws BankOperationException If amount is invalid or operation fails
     */
    public Transaction transfer(long fromAccountId, long toAccountId, BigDecimal amount)
            throws AccountNotFoundException, InsufficientFundsException, BankOperationException {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankOperationException("Transfer amount must be positive");
        }

        if (fromAccountId == toAccountId) {
            throw new BankOperationException("Cannot transfer to the same account");
        }

        Account fromAccount = accountRepository.findById(fromAccountId);
        if (fromAccount == null) {
            throw new AccountNotFoundException("Source account with ID " + fromAccountId + " not found");
        }

        Account toAccount = accountRepository.findById(toAccountId);
        if (toAccount == null) {
            throw new AccountNotFoundException("Destination account with ID " + toAccountId + " not found");
        }

        // Check sufficient funds
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        // Update source account balance
        BigDecimal newFromBalance = fromAccount.getBalance().subtract(amount);
        fromAccount.setBalance(newFromBalance);
        accountRepository.update(fromAccount);

        // Update destination account balance
        BigDecimal newToBalance = toAccount.getBalance().add(amount);
        toAccount.setBalance(newToBalance);
        accountRepository.update(toAccount);

        // Create transaction record for source account
        Transaction sourceTransaction = new Transaction();
        sourceTransaction.setAccountId(fromAccountId);
        sourceTransaction.setAmount(amount.negate()); // Negative amount for withdrawal
        sourceTransaction.setType("TRANSFER_OUT");
        sourceTransaction.setDescription("Transfer to account " + toAccountId);
        sourceTransaction.setTimestamp(new Date());
        sourceTransaction.setBalance(newFromBalance);
        sourceTransaction.setRelatedAccountId(toAccountId);

        // Create transaction record for destination account
        Transaction destTransaction = new Transaction();
        destTransaction.setAccountId(toAccountId);
        destTransaction.setAmount(amount); // Positive amount for deposit
        destTransaction.setType("TRANSFER_IN");
        destTransaction.setDescription("Transfer from account " + fromAccountId);
        destTransaction.setTimestamp(new Date());
        destTransaction.setBalance(newToBalance);
        destTransaction.setRelatedAccountId(fromAccountId);

        // Save both transactions
        Transaction savedSourceTransaction = transactionRepository.save(sourceTransaction);
        transactionRepository.save(destTransaction);

        // Log the transactions
        auditLogger.logTransaction(sourceTransaction);
        auditLogger.logTransaction(destTransaction);

        return savedSourceTransaction;
    }

    /**
     * Gets a transaction by its ID.
     *
     * @param transactionId The ID of the transaction
     * @return The transaction
     * @throws BankOperationException If transaction is not found
     */
    public Transaction getTransactionById(long transactionId) throws BankOperationException {
        Transaction transaction = transactionRepository.findById(transactionId);
        if (transaction == null) {
            throw new BankOperationException("Transaction with ID " + transactionId + " not found");
        }
        return transaction;
    }

    /**
     * Gets recent transactions for an account with pagination.
     *
     * @param accountId The ID of the account
     * @param page The page number (zero-based)
     * @param size The page size
     * @return List of transactions
     * @throws AccountNotFoundException If account is not found
     */
    public List<Transaction> getRecentTransactions(long accountId, int page, int size)
            throws AccountNotFoundException {

        if (!accountRepository.exists(accountId)) {
            throw new AccountNotFoundException("Account with ID " + accountId + " not found");
        }

        return transactionRepository.findByAccountIdPaginated(accountId, page, size);
    }

    /**
     * Gets transactions within a date range.
     *
     * @param accountId The ID of the account
     * @param startDate The start date
     * @param endDate The end date
     * @return List of transactions
     * @throws AccountNotFoundException If account is not found
     */
    public List<Transaction> getTransactionsByDateRange(long accountId, Date startDate, Date endDate)
            throws AccountNotFoundException {

        if (!accountRepository.exists(accountId)) {
            throw new AccountNotFoundException("Account with ID " + accountId + " not found");
        }

        return transactionRepository.findByAccountIdAndDateRange(accountId, startDate, endDate);
    }
}
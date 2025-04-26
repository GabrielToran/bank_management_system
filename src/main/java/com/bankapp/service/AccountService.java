package com.bankapp.service;
import java.util.List;
import com.bankapp.model.account.Account;
import com.bankapp.model.account.CheckingAccount;
import com.bankapp.model.account.LoanAccount;
import com.bankapp.model.account.SavingsAccount;
import com.bankapp.model.exception.AccountNotFoundException;
import com.bankapp.model.exception.BankOperationException;
import com.bankapp.model.exception.InsufficientFundsException;
import com.bankapp.model.repository.AccountRepository;
import com.bankapp.model.repository.TransactionRepository;
import com.bankapp.model.transaction.Transaction;
import com.bankapp.model.transaction.Transaction.TransactionType;
import com.bankapp.util.AuditLogger;

public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }
    public SavingsAccount createSavingsAccount(String customerId, double initialBalance, double interestRate, String username) {
        validateAmount(initialBalance, "Initial balance");

        SavingsAccount account = new SavingsAccount(customerId, initialBalance, interestRate);
        account.updateAuditInfo(username);

        boolean saved = accountRepository.save(account);
        if (!saved) {
            throw new BankOperationException("Failed to create savings account");
        }

        // Record the initial deposit transaction
        if (initialBalance > 0) {
            Transaction transaction = new Transaction(
                    account.getAccountId(),
                    TransactionType.DEPOSIT,
                    initialBalance,
                    "Initial deposit");
            transaction.updateAuditInfo(username);
            transactionRepository.save(transaction);
        }

        AuditLogger.log(username, "Created savings account: " + account.getAccountId());
        return account;
    }

    /**
     * Create a new checking account
     *
     * @param customerId The customer ID
     * @param initialBalance The initial balance
     * @param overdraftLimit The overdraft limit
     * @param overdraftFee The overdraft fee
     * @param username The username of the creator
     * @return The new account
     */
    public CheckingAccount createCheckingAccount(String customerId, double initialBalance,
                                                 double overdraftLimit, double overdraftFee, String username) {
        validateAmount(initialBalance, "Initial balance");

        CheckingAccount account = new CheckingAccount(customerId, initialBalance, overdraftLimit, overdraftFee);
        account.updateAuditInfo(username);

        boolean saved = accountRepository.save(account);
        if (!saved) {
            throw new BankOperationException("Failed to create checking account");
        }

        // Record the initial deposit transaction
        if (initialBalance > 0) {
            Transaction transaction = new Transaction(
                    account.getAccountId(),
                    TransactionType.DEPOSIT,
                    initialBalance,
                    "Initial deposit");
            transaction.updateAuditInfo(username);
            transactionRepository.save(transaction);
        }

        AuditLogger.log(username, "Created checking account: " + account.getAccountId());
        return account;
    }

    /**
     * Create a new loan account
     *
     * @param customerId The customer ID
     * @param loanAmount The loan amount
     * @param interestRate The interest rate
     * @param termMonths The loan term in months
     * @param username The username of the creator
     * @return The new account
     */
    public LoanAccount createLoanAccount(String customerId, double loanAmount,
                                         double interestRate, int termMonths, String username) {
        validateAmount(loanAmount, "Loan amount");

        LoanAccount account = new LoanAccount(customerId, loanAmount, interestRate, termMonths);
        account.updateAuditInfo(username);

        boolean saved = accountRepository.save(account);
        if (!saved) {
            throw new BankOperationException("Failed to create loan account");
        }

        // Record the loan disbursement transaction
        Transaction transaction = new Transaction(
                account.getAccountId(),
                TransactionType.DEPOSIT,
                loanAmount,
                "Loan disbursement");
        transaction.updateAuditInfo(username);
        transactionRepository.save(transaction);

        AuditLogger.log(username, "Created loan account: " + account.getAccountId());
        return account;
    }

    /**
     * Get account by ID
     *
     * @param accountId The account ID
     * @return The account
     * @throws AccountNotFoundException if account not found
     */
    public Account getAccount(String accountId) {
        return accountRepository.findById(accountId);
    }

    /**
     * Get accounts for a customer
     *
     * @param customerId The customer ID
     * @return List of accounts
     */
    public List<Account> getAccountsByCustomer(String customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    /**
     * Deposit money into an account
     *
     * @param accountId The account ID
     * @param amount The amount to deposit
     * @param description The transaction description
     * @param username The username of the depositor
     * @return The updated account
     * @throws AccountNotFoundException if account not found
     * @throws BankOperationException if deposit fails
     */
    public Account deposit(String accountId, double amount, String description, String username) {
        validateAmount(amount, "Deposit amount");

        Account account = accountRepository.findById(accountId);

        try {
            // Special handling for loan accounts
            if (account instanceof LoanAccount) {
                ((LoanAccount) account).makePayment(amount);
            } else {
                account.deposit(amount);
            }

            account.updateAuditInfo(username);
            accountRepository.update(account);

            // Record the transaction
            Transaction transaction = new Transaction(
                    accountId,
                    account instanceof LoanAccount ? TransactionType.PAYMENT : TransactionType.DEPOSIT,
                    amount,
                    description);
            transaction.updateAuditInfo(username);
            transactionRepository.save(transaction);

            AuditLogger.log(username, "Deposited " + amount + " to account: " + accountId);
            return account;

        } catch (Exception e) {
            throw new BankOperationException("Deposit failed: " + e.getMessage(), e);
        }
    }

    /**
     * Withdraw money from an account
     *
     * @param accountId The account ID
     * @param amount The amount to withdraw
     * @param description The transaction description
     * @param username The username of the withdrawer
     * @return The updated account
     * @throws AccountNotFoundException if account not found
     * @throws InsufficientFundsException if funds are insufficient
     * @throws BankOperationException if withdrawal fails
     */
    public Account withdraw(String accountId, double amount, String description, String username) {
        validateAmount(amount, "Withdrawal amount");

        Account account = accountRepository.findById(accountId);

        try {
            // Loan accounts cannot be withdrawn from
            if (account instanceof LoanAccount) {
                throw new UnsupportedOperationException("Cannot withdraw from a loan account");
            }

            account.withdraw(amount);
            account.updateAuditInfo(username);
            accountRepository.update();
            accountRepository.update(account);

            // Record the transaction
            Transaction transaction = new Transaction(
                    accountId,
                    TransactionType.WITHDRAWAL,
                    amount,
                    description);
            transaction.updateAuditInfo(username);
            transactionRepository.save(transaction);

            AuditLogger.log(username, "Withdrew " + amount + " from account: " + accountId);
            return account;

        } catch (InsufficientFundsException e) {
            throw e;  // Rethrow specific exception
        } catch (Exception e) {
            throw new BankOperationException("Withdrawal failed: " + e.getMessage(), e);
        }
    }

    /**
     * Transfer money between accounts
     *
     * @param fromAccountId The source account ID
     * @param toAccountId The target account ID
     * @param amount The amount to transfer
     * @param description The transaction description
     * @param username The username of the transferer
     * @return The updated source account
     * @throws AccountNotFoundException if either account not found
     * @throws InsufficientFundsException if funds are insufficient
     * @throws BankOperationException if transfer fails
     */
    public Account transfer(String fromAccountId, String toAccountId, double amount, String description, String username) {
        validateAmount(amount, "Transfer amount");

        Account fromAccount = accountRepository.findById(fromAccountId);
        Account toAccount = accountRepository.findById(toAccountId);

        try {
            // Loan accounts have special handling
            if (fromAccount instanceof LoanAccount) {
                throw new UnsupportedOperationException("Cannot transfer from a loan account");
            }

            // Withdraw from source account
            fromAccount.withdraw(amount);
            fromAccount.updateAuditInfo(username);
            accountRepository.update(fromAccount);

            // Deposit to target account (with special handling for loan)
            if (toAccount instanceof LoanAccount) {
                ((LoanAccount) toAccount).makePayment(amount);
            } else {
                toAccount.deposit(amount);
            }
            toAccount.updateAuditInfo(username);
            accountRepository.update(toAccount);

            // Record the transfer transactions
            Transaction fromTransaction = Transaction.createTransfer(
                    fromAccountId,
                    toAccountId,
                    amount,
                    description);
            fromTransaction.updateAuditInfo(username);
            transactionRepository.save(fromTransaction);

            // Create a corresponding deposit transaction for the target account
            Transaction toTransaction = Transaction.createTransfer(
                    toAccountId,
                    fromAccountId,
                    amount,
                    description + " (from " + fromAccountId + ")");
            toTransaction.updateAuditInfo(username);
            transactionRepository.save(toTransaction);

            AuditLogger.log(username, "Transferred " + amount + " from account: " + fromAccountId + " to account: " + toAccountId);
            return fromAccount;

        } catch (InsufficientFundsException e) {
            throw e;  // Rethrow specific exception
        } catch (Exception e) {
            throw new BankOperationException("Transfer failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get transactions for an account
     *
     * @param accountId The account ID
     * @return List of transactions
     */
    public List<Transaction> getTransactions(String accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    /**
     * Close an account
     *
     * @param accountId The account ID
     * @param username The username of the closer
     * @return true if successful
     * @throws AccountNotFoundException if account not found
     * @throws BankOperationException if balance is not zero or close fails
     */
    public boolean closeAccount(String accountId, String username) {
        Account account = accountRepository.findById(accountId);

        // Cannot close account with balance
        if (account.getBalance() != 0) {
            throw new BankOperationException("Cannot close account with non-zero balance: " + account.getBalance());
        }

        boolean deleted = accountRepository.delete(accountId);
        if (deleted) {
            AuditLogger.log(username, "Closed account: " + accountId);
        }

        return deleted;
    }

    /**
     * Calculate interest for an account
     *
     * @param accountId The account ID
     * @param username The username of the processor
     * @return The updated account
     */
    public Account calculateInterest(String accountId, String username) {
        Account account = accountRepository.findById(accountId);

        // Only calculate interest for appropriate account types
        if (account instanceof SavingsAccount || account instanceof LoanAccount) {
            double oldBalance = account.getBalance();
            account.calculateInterest();
            double interestAmount = Math.abs(account.getBalance() - oldBalance);

            account.updateAuditInfo(username);
            accountRepository.update(account);

            // Record the interest transaction
            TransactionType type = account instanceof SavingsAccount ?
                    TransactionType.INTEREST : TransactionType.FEE;

            Transaction transaction = new Transaction(
                    accountId,
                    type,
                    interestAmount,
                    "Interest calculation");
            transaction.updateAuditInfo(username);
            transactionRepository.save(transaction);

            AuditLogger.log(username, "Calculated interest for account: " + accountId);
        }

        return account;
    }

    /**
     * Validate that an amount is positive
     *
     * @param amount The amount to validate
     * @param fieldName The name of the field
     * @throws IllegalArgumentException if amount is not positive
     */
    private void validateAmount(double amount, String fieldName) {
        if (amount <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
}

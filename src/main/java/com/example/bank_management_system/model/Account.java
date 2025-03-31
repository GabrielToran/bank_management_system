package com.example.bank_management_system.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Account class
public class Account {
    private String accountNumber;
    private static String customerId;
    private AccountType type;
    private BigDecimal balance;
    private List<Transaction> transactions;

    public enum AccountType {
        CHECKING, SAVINGS, CREDIT
    }

    public Account(String customerId, AccountType type) {
        this.accountNumber = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.type = type;
        this.balance = BigDecimal.ZERO;
        this.transactions = new ArrayList<>();
    }

    // Getters and setters
    public String getAccountNumber() { return accountNumber; }
    public static String getCustomerId() { return customerId; }
    public AccountType getType() { return type; }
    public BigDecimal getBalance() { return balance; }
    public List<Transaction> getTransactions() { return transactions; }

    // Core account functionality
    public void deposit(BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        balance = balance.add(amount);
        transactions.add(new Transaction(accountNumber, TransactionType.DEPOSIT, amount, description));
    }

    public void withdraw(BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        balance = balance.subtract(amount);
        transactions.add(new Transaction(accountNumber, TransactionType.WITHDRAWAL, amount, description));
    }

    @Override
    public String toString() {
        return "Account [Number=" + accountNumber + ", Type=" + type +
                ", Balance=" + balance + "]";
    }
}

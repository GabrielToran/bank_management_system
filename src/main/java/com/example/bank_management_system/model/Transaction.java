package com.example.bank_management_system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Transaction class
enum TransactionType {
    DEPOSIT, WITHDRAWAL, TRANSFER_OUT, TRANSFER_IN, PAYMENT
}

 public class Transaction {
    private String transactionId;
    private String accountNumber;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;

    public Transaction(String accountNumber, TransactionType type, BigDecimal amount, String description) {
        this.transactionId = UUID.randomUUID().toString();
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getAccountNumber() { return accountNumber; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Transaction [ID=" + transactionId + ", Type=" + type +
                ", Amount=" + amount + ", Time=" + timestamp +
                ", Description=" + description + "]";
    }
}
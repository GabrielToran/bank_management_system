package com.bankapp.model.transaction;

import java.time.LocalDateTime;
import java.util.UUID;

import com.bankapp.model.interfaces.Auditable;

/**
 * Class representing a transaction in the system
 */
public class Transaction implements Auditable {
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, FEE, INTEREST
    }

    private String transactionId;
    private String accountId;
    private TransactionType type;
    private double amount;
    private String description;
    private LocalDateTime timestamp;

    // Additional reference for transfers
    private String targetAccountId;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private String lastModifiedBy;

    /**
     * Creates a new Transaction
     *
     * @param accountId The account ID
     * @param type The transaction type
     * @param amount The transaction amount
     * @param description The transaction description
     */
    public Transaction(String accountId, TransactionType type, double amount, String description) {
        this.transactionId = UUID.randomUUID().toString();
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * Creates a transfer transaction
     *
     * @param sourceAccountId The source account ID
     * @param targetAccountId The target account ID
     * @param amount The transfer amount
     * @param description The transaction description
     * @return A new transfer transaction
     */
    public static Transaction createTransfer(String sourceAccountId, String targetAccountId, double amount, String description) {
        Transaction transaction = new Transaction(sourceAccountId, TransactionType.TRANSFER, amount, description);
        transaction.targetAccountId = targetAccountId;
        return transaction;
    }

    // Getters
    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getTargetAccountId() {
        return targetAccountId;
    }

    // Auditable implementation
    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    @Override
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    @Override
    public void updateAuditInfo(String modifiedBy) {
        this.lastUpdatedAt = LocalDateTime.now();
        this.lastModifiedBy = modifiedBy;
    }
}

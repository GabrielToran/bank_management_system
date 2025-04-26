package com.bankapp.model.repository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bankapp.model.exception.BankOperationException;
import com.bankapp.model.transaction.Transaction;
import com.bankapp.model.transaction.Transaction.TransactionType;
import com.bankapp.util.DatabaseUtil;

public class TransactionRepository {
    public boolean save(Transaction transaction) {
        String sql = "INSERT INTO transactions (transaction_id, account_id, type, amount, " +
                "description, timestamp, target_account_id, created_at, last_updated_at, last_modified_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transaction.getTransactionId());
            pstmt.setString(2, transaction.getAccountId());
            pstmt.setString(3, transaction.getType().toString());
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setString(5, transaction.getDescription());
            pstmt.setObject(6, transaction.getTimestamp());
            pstmt.setString(7, transaction.getTargetAccountId());
            pstmt.setObject(8, transaction.getCreatedAt());
            pstmt.setObject(9, transaction.getLastUpdatedAt());
            pstmt.setString(10, transaction.getLastModifiedBy());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new BankOperationException("Error saving transaction: " + transaction.getTransactionId(), e);
        }
    }

    /**
     * Find transactions by account ID
     *
     * @param accountId The account ID
     * @return List of transactions for the account
     */
    public List<Transaction> findByAccountId(String accountId) {
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY timestamp DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new BankOperationException("Error finding transactions for account: " + accountId, e);
        }

        return transactions;
    }

    /**
     * Find transactions for a customer across all their accounts
     *
     * @param customerId The customer ID
     * @return List of transactions
     */
    public List<Transaction> findByCustomerId(String customerId) {
        String sql = "SELECT t.* FROM transactions t " +
                "JOIN accounts a ON t.account_id = a.account_id " +
                "WHERE a.customer_id = ? ORDER BY t.timestamp DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new BankOperationException("Error finding transactions for customer: " + customerId, e);
        }

        return transactions;
    }

    /**
     * Find transactions by date range
     *
     * @param accountId The account ID
     * @param startDate The start date
     * @param endDate The end date
     * @return List of transactions in the date range
     */
    public List<Transaction> findByDateRange(String accountId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT * FROM transactions WHERE account_id = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountId);
            pstmt.setObject(2, startDate);
            pstmt.setObject(3, endDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new BankOperationException("Error finding transactions by date range for account: " + accountId, e);
        }

        return transactions;
    }

    /**
     * Map a ResultSet row to a Transaction object
     *
     * @param rs The ResultSet
     * @return A new Transaction object
     * @throws SQLException if mapping fails
     */
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        String transactionId = rs.getString("transaction_id");
        String accountId = rs.getString("account_id");
        TransactionType type = TransactionType.valueOf(rs.getString("type"));
        double amount = rs.getDouble("amount");
        String description = rs.getString("description");
        String targetAccountId = rs.getString("target_account_id");

        Transaction transaction;
        if (type == TransactionType.TRANSFER && targetAccountId != null) {
            transaction = Transaction.createTransfer(accountId, targetAccountId, amount, description);
        } else {
            transaction = new Transaction(accountId, type, amount, description);
        }

        // We'd need to use reflection or another method to properly set these fields
        // This is a simplified example
        return transaction;
    }
}

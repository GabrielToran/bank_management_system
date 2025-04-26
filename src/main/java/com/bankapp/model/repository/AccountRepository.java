package com.bankapp.model.repository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bankapp.model.account.Account;
import com.bankapp.model.account.CheckingAccount;
import com.bankapp.model.account.LoanAccount;
import com.bankapp.model.account.SavingsAccount;
import com.bankapp.model.exception.AccountNotFoundException;
import com.bankapp.model.exception.BankOperationException;
import com.bankapp.util.DatabaseUtil;

public class AccountRepository {
    public Account findById(String accountId) {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                }
            }
        } catch (SQLException e) {
            throw new BankOperationException("Error finding account by ID: " + accountId, e);
        }

        throw new AccountNotFoundException("Account not found with ID: " + accountId, accountId);
    }

    /**
     * Find accounts by customer ID
     *
     * @param customerId The customer ID
     * @return List of accounts for the customer
     */
    public List<Account> findByCustomerId(String customerId) {
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
        List<Account> accounts = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new BankOperationException("Error finding accounts for customer: " + customerId, e);
        }

        return accounts;
    }

    /**
     * Save an account to the database
     *
     * @param account The account to save
     * @return true if successful
     */
    public boolean save(Account account) {
        String sql = "INSERT INTO accounts (account_id, customer_id, account_type, balance, " +
                "interest_rate, overdraft_limit, overdraft_fee, term_months, original_loan_amount, " +
                "monthly_payment, created_at, last_updated_at, last_modified_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getAccountId());
            pstmt.setString(2, account.getCustomerId());

            // Set account type based on class
            String accountType;
            double interestRate = 0.0;
            double overdraftLimit = 0.0;
            double overdraftFee = 0.0;
            int termMonths = 0;
            double originalLoanAmount = 0.0;
            double monthlyPayment = 0.0;

            if (account instanceof SavingsAccount) {
                accountType = "SAVINGS";
                interestRate = ((SavingsAccount) account).getInterestRate();
            } else if (account instanceof CheckingAccount) {
                accountType = "CHECKING";
                overdraftLimit = ((CheckingAccount) account).getOverdraftLimit();
                overdraftFee = ((CheckingAccount) account).getOverdraftFee();
            } else if (account instanceof LoanAccount) {
                accountType = "LOAN";
                interestRate = ((LoanAccount) account).getInterestRate();
                termMonths = ((LoanAccount) account).getTermMonths();
                originalLoanAmount = ((LoanAccount) account).getOriginalLoanAmount();
                monthlyPayment = ((LoanAccount) account).getMonthlyPayment();
            } else {
                throw new BankOperationException("Unknown account type");
            }

            pstmt.setString(3, accountType);
            pstmt.setDouble(4, account.getBalance());
            pstmt.setDouble(5, interestRate);
            pstmt.setDouble(6, overdraftLimit);
            pstmt.setDouble(7, overdraftFee);
            pstmt.setInt(8, termMonths);
            pstmt.setDouble(9, originalLoanAmount);
            pstmt.setDouble(10, monthlyPayment);
            pstmt.setObject(11, account.getCreatedAt());
            pstmt.setObject(12, account.getLastUpdatedAt());
            pstmt.setString(13, account.getLastModifiedBy());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new BankOperationException("Error saving account: " + account.getAccountId(), e);
        }
    }

    /**
     * Update an existing account
     *
     * @param account The account to update
     * @return true if successful
     */
    public boolean update(Account account) {
        String sql = "UPDATE accounts SET balance = ?, interest_rate = ?, overdraft_limit = ?, " +
                "overdraft_fee = ?, last_updated_at = ?, last_modified_by = ? WHERE account_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, account.getBalance());

            // Set type-specific fields
            double interestRate = 0.0;
            double overdraftLimit = 0.0;
            double overdraftFee = 0.0;

            if (account instanceof SavingsAccount) {
                interestRate = ((SavingsAccount) account).getInterestRate();
            } else if (account instanceof CheckingAccount) {
                overdraftLimit = ((CheckingAccount) account).getOverdraftLimit();
                overdraftFee = ((CheckingAccount) account).getOverdraftFee();
            } else if (account instanceof LoanAccount) {
                interestRate = ((LoanAccount) account).getInterestRate();
            }

            pstmt.setDouble(2, interestRate);
            pstmt.setDouble(3, overdraftLimit);
            pstmt.setDouble(4, overdraftFee);
            pstmt.setObject(5, account.getLastUpdatedAt());
            pstmt.setString(6, account.getLastModifiedBy());
            pstmt.setString(7, account.getAccountId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new BankOperationException("Error updating account: " + account.getAccountId(), e);
        }
    }

    /**
     * Delete an account
     *
     * @param accountId The ID of the account to delete
     * @return true if successful
     */
    public boolean delete(String accountId) {
        String sql = "DELETE FROM accounts WHERE account_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new BankOperationException("Error deleting account: " + accountId, e);
        }
    }

    /**
     * Map a ResultSet row to an Account object
     *
     * @param rs The ResultSet
     * @return A new Account object
     * @throws SQLException if mapping fails
     */
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        String accountId = rs.getString("account_id");
        String customerId = rs.getString("customer_id");
        String accountType = rs.getString("account_type");
        double balance = rs.getDouble("balance");
        double interestRate = rs.getDouble("interest_rate");
        double overdraftLimit = rs.getDouble("overdraft_limit");
        double overdraftFee = rs.getDouble("overdraft_fee");
        int termMonths = rs.getInt("term_months");
        double originalLoanAmount = rs.getDouble("original_loan_amount");
        LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
        LocalDateTime lastUpdatedAt = rs.getObject("last_updated_at", LocalDateTime.class);
        String lastModifiedBy = rs.getString("last_modified_by");

        Account account;

        switch (accountType) {
            case "SAVINGS":
                account = new SavingsAccount(customerId, balance, interestRate);
                break;
            case "CHECKING":
                account = new CheckingAccount(customerId, balance, overdraftLimit, overdraftFee);
                break;
            case "LOAN":
                account = new LoanAccount(customerId, originalLoanAmount, interestRate, termMonths);
                // Override the balance as it might have been paid down
                ((LoanAccount) account).deposit(originalLoanAmount - balance);
                break;
            default:
                throw new SQLException("Unknown account type: " + accountType);
        }

        // Set common field values that are not in constructor
        // Reflection would be cleaner but using direct casting for simplicity

        // We'd need to use reflection or another method to properly set these fields
        // This is a simplified example
        return account;
    }
}

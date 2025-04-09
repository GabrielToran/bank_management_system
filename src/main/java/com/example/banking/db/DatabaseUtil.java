package com.example.banking.db;
import java.sql.*;

public class DatabaseUtil {
    // H2 database connection settings
    private static final String DB_URL = "jdbc:h2:./bankingdb";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private static DatabaseUtil instance;

    static {
        try {
            // Load H2 JDBC driver
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 JDBC Driver not found", e);
        }
    }

    private DatabaseUtil() {
        // Private constructor for singleton pattern
    }

    public static synchronized DatabaseUtil getInstance() {
        if (instance == null) {
            instance = new DatabaseUtil();
        }
        return instance;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            // Create table for single-client app (without users table)
            // Create accounts table (without user reference)
            stmt.execute("CREATE TABLE IF NOT EXISTS account (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "account_number VARCHAR(20) UNIQUE NOT NULL, " +
                    "balance DECIMAL(15, 2) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Create transactions table
            stmt.execute("CREATE TABLE IF NOT EXISTS transaction (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "account_id BIGINT NOT NULL, " +
                    "type VARCHAR(20) NOT NULL, " +
                    "amount DECIMAL(15, 2) NOT NULL, " +
                    "description VARCHAR(255), " +
                    "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (account_id) REFERENCES account(id))");

            // Add default account if none exists (for single-client approach)
            ResultSet checkResult = stmt.executeQuery(
                    "SELECT COUNT(*) AS count FROM account");

            if (checkResult.next() && checkResult.getInt("count") == 0) {
                stmt.execute(
                        "INSERT INTO account (account_number, balance) VALUES ('1000000001', 1000.00)");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }
}
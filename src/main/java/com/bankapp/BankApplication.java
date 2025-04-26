package com.bankapp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bankapp.model.repository.AccountRepository;
import com.bankapp.model.repository.CustomerRepository;
import com.bankapp.model.repository.TransactionRepository;
import com.bankapp.service.AccountService;
import com.bankapp.service.CustomerService;
import com.bankapp.service.TransactionService;
import com.bankapp.util.DatabaseUtil;
import com.bankapp.util.AuditLogger;

/**
 * Main application class for the Bank Management System.
 * Initializes the application, sets up repositories and services,
 * and provides access to them throughout the application.
 */
public class BankApplication {
    private static final Logger LOGGER = Logger.getLogger(BankApplication.class.getName());

    private static BankApplication instance;

    // Repositories
    private AccountRepository accountRepository;
    private CustomerRepository customerRepository;
    private TransactionRepository transactionRepository;

    // Services
    private AccountService accountService;
    private CustomerService customerService;
    private TransactionService transactionService;

    // Utilities
    private DatabaseUtil databaseUtil;
    private AuditLogger auditLogger;

    /**
     * Private constructor for singleton pattern
     */
    private BankApplication() {
        initialize();
    }

    /**
     * Gets the singleton instance of the application
     * @return BankApplication instance
     */
    public static synchronized BankApplication getInstance() {
        if (instance == null) {
            instance = new BankApplication();
        }
        return instance;
    }

    /**
     * Initializes the application components
     */
    private void initialize() {
        LOGGER.info("Initializing Bank Management System");

        try {
            // Initialize utilities
            databaseUtil = new DatabaseUtil();
            auditLogger = new AuditLogger();

            // Initialize database connection
            Connection connection = databaseUtil.getConnection();

            // Initialize repositories
            accountRepository = new AccountRepository(connection);
            customerRepository = new CustomerRepository(connection);
            transactionRepository = new TransactionRepository(connection);

            // Initialize services
            accountService = new AccountService(accountRepository, transactionRepository);
            customerService = new CustomerService(customerRepository);
            transactionService = new TransactionService(transactionRepository, accountRepository, auditLogger);

            LOGGER.info("Bank Management System initialized successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Bank Management System", e);
            throw new RuntimeException("Failed to initialize application", e);
        }
    }

    /**
     * Gets the account service
     * @return AccountService
     */
    public AccountService getAccountService() {
        return accountService;
    }

    /**
     * Gets the customer service
     * @return CustomerService
     */
    public CustomerService getCustomerService() {
        return customerService;
    }

    /**
     * Gets the transaction service
     * @return TransactionService
     */
    public TransactionService getTransactionService() {
        return transactionService;
    }

    /**
     * Gets the database utility
     * @return DatabaseUtil
     */
    public DatabaseUtil getDatabaseUtil() {
        return databaseUtil;
    }

    /**
     * Gets the audit logger
     * @return AuditLogger
     */
    public AuditLogger getAuditLogger() {
        return auditLogger;
    }

    /**
     * Application shutdown hook
     */
    public void shutdown() {
        LOGGER.info("Shutting down Bank Management System");

        try {
            if (databaseUtil != null) {
                databaseUtil.closeConnection();
            }
            LOGGER.info("Bank Management System shutdown completed");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during application shutdown", e);
        }
    }

    /**
     * Main method to start the application
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            // Get the application instance
            BankApplication app = BankApplication.getInstance();

            // Register shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    app.shutdown();
                }
            });

            LOGGER.info("Bank Management System started successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start Bank Management System", e);
        }
    }
}
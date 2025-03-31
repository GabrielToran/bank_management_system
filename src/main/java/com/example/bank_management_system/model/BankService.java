package com.example.bank_management_system.model;

import java.math.BigDecimal;
import java.util.*;

// Bank service class to manage accounts and customers
public class BankService {
    private Map<String, Customer> customers;
    private Map<String, Account> accounts;
    private PaymentGatewayConnector paymentGateway;
    private AIChatbotConnector chatbot;

    public BankService() {
        customers = new HashMap<>();
        accounts = new HashMap<>();
        paymentGateway = new PaymentGatewayConnector();
        chatbot = new AIChatbotConnector();
    }

    // FEATURE 1: Customer Account Management
    public Customer createCustomer(String firstName, String lastName, String email, String phoneNumber) {
        Customer customer = new Customer(firstName, lastName, email, phoneNumber);
        customers.put(customer.getCustomerId(), customer);
        return customer;
    }

    public Account createAccount(String customerId, Account.AccountType type) {
        if (!customers.containsKey(customerId)) {
            throw new IllegalArgumentException("Customer does not exist");
        }

        Account account = new Account(customerId, type);
        accounts.put(account.getAccountNumber(), account);
        return account;
    }

    // FEATURE 2: Transaction Processing
    public void processDeposit(String accountNumber, BigDecimal amount, String description) {
        Account account = getAccountByNumber(accountNumber);
        account.deposit(amount, description);
    }

    public void processWithdrawal(String accountNumber, BigDecimal amount, String description) {
        Account account = getAccountByNumber(accountNumber);
        account.withdraw(amount, description);
    }

    // Process payment via payment gateway
    public boolean processPayment(String accountNumber, String recipientIdentifier, BigDecimal amount, String description) {
        Account account = getAccountByNumber(accountNumber);

        // First verify we have the funds
        if (account.getBalance().compareTo(amount) < 0) {
            return false;
        }

        // Call payment gateway API to process the payment
        boolean paymentSuccess = processPayment(accountNumber, recipientIdentifier, amount, description);

        if (paymentSuccess) {
            // Deduct from our account
            account.withdraw(amount, "Payment to " + recipientIdentifier + ": " + description);
            return true;
        }

        return false;
    }

    // FEATURE 3: Account Balance Inquiry
    public BigDecimal getAccountBalance(String accountNumber) {
        return getAccountByNumber(accountNumber).getBalance();
    }

    public List<Transaction> getAccountTransactions(String accountNumber) {
        return getAccountByNumber(accountNumber).getTransactions();
    }

    // FEATURE 4: Fund Transfers
    public void transferFunds(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description) {
        Account fromAccount = getAccountByNumber(fromAccountNumber);
        Account toAccount = getAccountByNumber(toAccountNumber);

        // Check for sufficient funds
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for transfer");
        }

        // Perform the transfer
        fromAccount.withdraw(amount, "Transfer to " + toAccountNumber + ": " + description);
        fromAccount.getTransactions().add(new Transaction(fromAccountNumber, TransactionType.TRANSFER_OUT, amount, description));

        toAccount.deposit(amount, "Transfer from " + fromAccountNumber + ": " + description);
        toAccount.getTransactions().add(new Transaction(toAccountNumber, TransactionType.TRANSFER_IN, amount, description));
    }

    // Helper method to fetch account
    private Account getAccountByNumber(String accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        return account;
    }

    // AI Chatbot integration
    public String processChatbotQuery(String customerId, String query) {
        Customer customer = customers.get(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        // Fetch customer's accounts for context
        List<Account> customerAccounts = new ArrayList<>();
        for (Account account : accounts.values()) {
            if (account.getCustomerId().equals(customerId)) {
                customerAccounts.add(account);
            }
        }

        // Process the query through the AI chatbot with customer context
        return chatbot.processQuery(customer, customerAccounts, query);
    }

    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }
    public boolean validateCustomer(String customerId, String password) {
        Customer customer = customers.get(customerId);
        if (customer == null) {
            return false;
        }
        // In a real application, you would hash the password and compare it properly
        // This is just for demonstration
        return customer.getPassword().equals(password);
    }
}
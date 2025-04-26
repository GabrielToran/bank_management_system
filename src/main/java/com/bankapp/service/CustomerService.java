package com.bankapp.service;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.bankapp.model.account.Account;
import com.bankapp.model.exception.BankOperationException;
import com.bankapp.model.person.Customer;
import com.bankapp.model.repository.AccountRepository;
import com.bankapp.model.repository.CustomerRepository;
import com.bankapp.model.repository.TransactionRepository;
import com.bankapp.model.transaction.Transaction;
import com.bankapp.util.AuditLogger;

public class CustomerService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Creates a new CustomerService
     *
     * @param customerRepository The customer repository
     * @param accountRepository The account repository
     * @param transactionRepository The transaction repository
     */
    public CustomerService(CustomerRepository customerRepository, AccountRepository accountRepository,
                           TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Create a new customer
     *
     * @param firstName The first name
     * @param lastName The last name
     * @param email The email address
     * @param phone The phone number
     * @param dateOfBirth The date of birth
     * @param address The physical address
     * @param username The username for login
     * @param password The password for login
     * @param createdBy The username of the creator
     * @return The new customer
     */
    public Customer createCustomer(String firstName, String lastName, String email,
                                   String phone, LocalDate dateOfBirth, String address,
                                   String username, String password, String createdBy) {

        // Validate input
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Check if username already exists
        Customer existingCustomer = customerRepository.findByUsername(username);
        if (existingCustomer != null) {
            throw new BankOperationException("Username already exists: " + username);
        }

        // Create the customer
        String id = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        Customer customer = new Customer(id, firstName, lastName, email, phone,
                dateOfBirth, address, customerId, username, password);
        customer.updateAuditInfo(createdBy);

        boolean saved = customerRepository.save(customer);
        if (!saved) {
            throw new BankOperationException("Failed to create customer");
        }

        AuditLogger.log(createdBy, "Created customer: " + customer.getCustomerId());
        return customer;
    }

    /**
     * Get customer by ID
     *
     * @param customerId The customer ID
     * @return The customer or null if not found
     */
    public Customer getCustomer(String customerId) {
        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            throw new BankOperationException("Customer not found with ID: " + customerId);
        }

        // Load accounts for this customer
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        accounts.forEach(customer::addAccount);

        return customer;
    }

    /**
     * Get customer by username
     *
     * @param username The username
     * @return The customer or null if not found
     */
    public Customer getCustomerByUsername(String username) {
        Customer customer = customerRepository.findByUsername(username);
        if (customer == null) {
            throw new BankOperationException("Customer not found with username: " + username);
        }

        // Load accounts for this customer
        List<Account> accounts = accountRepository.findByCustomerId(customer.getCustomerId());
        accounts.forEach(customer::addAccount);

        return customer;
    }

    /**
     * Update customer information
     *
     * @param customer The customer to update
     * @param modifiedBy The username of the modifier
     * @return The updated customer
     */
    public Customer updateCustomer(Customer customer, String modifiedBy) {
        // Validate input
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        customer.updateAuditInfo(modifiedBy);

        boolean updated = customerRepository.update(customer);
        if (!updated) {
            throw new BankOperationException("Failed to update customer: " + customer.getCustomerId());
        }

        AuditLogger.log(modifiedBy, "Updated customer: " + customer.getCustomerId());
        return customer;
    }

    /**
     * Change customer password
     *
     * @param customerId The customer ID
     * @param oldPassword The old password
     * @param newPassword The new password
     * @param modifiedBy The username of the modifier
     * @return The updated customer
     */
    public Customer changePassword(String customerId, String oldPassword, String newPassword, String modifiedBy) {
        Customer customer = getCustomer(customerId);

        // Validate old password
        if (!customer.validatePassword(oldPassword)) {
            throw new BankOperationException("Invalid old password");
        }

        // Validate new password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        customer.setPassword(newPassword);
        customer.updateAuditInfo(modifiedBy);

        boolean updated = customerRepository.update(customer);
        if (!updated) {
            throw new BankOperationException("Failed to update password for customer: " + customerId);
        }

        AuditLogger.log(modifiedBy, "Changed password for customer: " + customerId);
        return customer;
    }

    /**
     * Get transactions for a customer across all accounts
     *
     * @param customerId The customer ID
     * @return List of transactions
     */
    public List<Transaction> getCustomerTransactions(String customerId) {
        return transactionRepository.findByCustomerId(customerId);
    }

    /**
     * Authenticate a customer
     *
     * @param username The username
     * @param password The password
     * @return The authenticated customer or null if authentication fails
     */
    public Customer authenticate(String username, String password) {
        Customer customer = customerRepository.findByUsername(username);

        if (customer != null && customer.validatePassword(password)) {
            // Load accounts for this customer
            List<Account> accounts = accountRepository.findByCustomerId(customer.getCustomerId());
            accounts.forEach(customer::addAccount);

            AuditLogger.log(username, "Customer logged in: " + customer.getCustomerId());
            return customer;
        }

        return null;
    }
}

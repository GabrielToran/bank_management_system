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
import com.bankapp.model.entity.CustomerEntity;
import com.bankapp.model.exception.CustomerNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class CustomerService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EntityManager entityManager;


    public CustomerService(CustomerRepository customerRepository, AccountRepository accountRepository,
                           TransactionRepository transactionRepository, EntityManager entityManager) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.entityManager = entityManager;
    }

    public CustomerService(CustomerRepository customerRepository) {

    }


    public CustomerEntity createCustomer(CustomerEntity customer, String username) {
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            
            // Check if username or email already exists
            if (customerRepository.findByUsername(customer.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username already exists");
            }
            if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email already exists");
            }

            customer.updateAuditInfo(username);
            customer = customerRepository.save(customer);

            tx.commit();
            AuditLogger.log(username, "Created customer: " + customer.getCustomerId());
            return customer;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    /**
     * Get customer by ID
     *
     * @param customerId The customer ID
     * @return The customer or null if not found
     */
    public CustomerEntity getCustomerById(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));
    }

    /**
     * Get customer by username
     *
     * @param username The username
     * @return The customer or null if not found
     */
    public CustomerEntity getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with username: " + username));
    }


    public CustomerEntity updateCustomer(CustomerEntity customer, String username) {
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            
            // Verify customer exists
            CustomerEntity existingCustomer = getCustomerById(customer.getCustomerId());
            
            // Check if new username or email conflicts with other customers
            if (!existingCustomer.getUsername().equals(customer.getUsername()) &&
                customerRepository.findByUsername(customer.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username already exists");
            }
            if (!existingCustomer.getEmail().equals(customer.getEmail()) &&
                customerRepository.findByEmail(customer.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email already exists");
            }

            customer.updateAuditInfo(username);
            customer = customerRepository.save(customer);

            tx.commit();
            AuditLogger.log(username, "Updated customer: " + customer.getCustomerId());
            return customer;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
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
    public CustomerEntity changePassword(String customerId, String oldPassword, String newPassword, String modifiedBy) {
        CustomerEntity customer = getCustomerById(customerId);

        // Validate old password
        if (!customer.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("Invalid old password");
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
    public CustomerEntity authenticate(String username, String password) {
        CustomerEntity customer = getCustomerByUsername(username);
        if (!customer.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid password");
        }
        return customer;
    }

    public void deleteCustomer(String customerId, String username) {
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            
            CustomerEntity customer = getCustomerById(customerId);
            customerRepository.delete(customerId);

            tx.commit();
            AuditLogger.log(username, "Deleted customer: " + customerId);
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public List<CustomerEntity> getAllCustomers() {
        return customerRepository.findAll();
    }
}

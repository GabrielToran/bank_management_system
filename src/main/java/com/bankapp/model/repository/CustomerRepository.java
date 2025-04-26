package com.bankapp.model.repository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.bankapp.model.exception.BankOperationException;
import com.bankapp.model.person.Customer;
import com.bankapp.util.DatabaseUtil;

public class CustomerRepository {
    public Customer findById(String customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            throw new BankOperationException("Error finding customer by ID: " + customerId, e);
        }

        return null;
    }

    /**
     * Find a customer by username
     *
     * @param username The username
     * @return The customer or null if not found
     */
    public Customer findByUsername(String username) {
        String sql = "SELECT * FROM customers WHERE username = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            throw new BankOperationException("Error finding customer by username: " + username, e);
        }

        return null;
    }

    /**
     * Save a customer to the database
     *
     * @param customer The customer to save
     * @return true if successful
     */
    public boolean save(Customer customer) {
        String sql = "INSERT INTO customers (id, first_name, last_name, email, phone, " +
                "date_of_birth, address, customer_id, username, password, created_at, " +
                "last_updated_at, last_modified_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getId());
            pstmt.setString(2, customer.getFirstName());
            pstmt.setString(3, customer.getLastName());
            pstmt.setString(4, customer.getEmail());
            pstmt.setString(5, customer.getPhone());
            pstmt.setObject(6, customer.getDateOfBirth());
            pstmt.setString(7, customer.getAddress());
            pstmt.setString(8, customer.getCustomerId());
            pstmt.setString(9, customer.getUsername());
            pstmt.setString(10, customer.getPassword());
            pstmt.setObject(11, customer.getCreatedAt());
            pstmt.setObject(12, customer.getLastUpdatedAt());
            pstmt.setString(13, customer.getLastModifiedBy());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new BankOperationException("Error saving customer: " + customer.getCustomerId(), e);
        }
    }

    /**
     * Update an existing customer
     *
     * @param customer The customer to update
     * @return true if successful
     */
    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET first_name = ?, last_name = ?, email = ?, " +
                "phone = ?, address = ?, username = ?, password = ?, " +
                "last_updated_at = ?, last_modified_by = ? WHERE customer_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getFirstName());
            pstmt.setString(2, customer.getLastName());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getPhone());
            pstmt.setString(5, customer.getAddress());
            pstmt.setString(6, customer.getUsername());
            pstmt.setString(7, customer.getPassword());
            pstmt.setObject(8, customer.getLastUpdatedAt());
            pstmt.setString(9, customer.getLastModifiedBy());
            pstmt.setString(10, customer.getCustomerId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new BankOperationException("Error updating customer: " + customer.getCustomerId(), e);
        }
    }

    /**
     * Delete a customer
     *
     * @param customerId The ID of the customer to delete
     * @return true if successful
     */
    public boolean delete(String customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new BankOperationException("Error deleting customer: " + customerId, e);
        }
    }

    /**
     * Get all customers
     *
     * @return List of all customers
     */
    public List<Customer> findAll() {
        String sql = "SELECT * FROM customers";
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }

        } catch (SQLException e) {
            throw new BankOperationException("Error retrieving all customers", e);
        }

        return customers;
    }

    /**
     * Map a ResultSet row to a Customer object
     *
     * @param rs The ResultSet
     * @return A new Customer object
     * @throws SQLException if mapping fails
     */
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        LocalDate dateOfBirth = rs.getObject("date_of_birth", LocalDate.class);
        String address = rs.getString("address");
        String customerId = rs.getString("customer_id");
        String username = rs.getString("username");
        String password = rs.getString("password");

        return new Customer(id, firstName, lastName, email, phone, dateOfBirth,
                address, customerId, username, password);
    }
        }

package com.bankapp.model.repository;

import com.bankapp.model.entity.CustomerEntity;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    void setConnection(Connection connection);
    Optional<CustomerEntity> findById(String customerId);
    Optional<CustomerEntity> findByUsername(String username);
    Optional<CustomerEntity> findByEmail(String email);
    CustomerEntity save(CustomerEntity customer);
    void delete(String customerId);
    List<CustomerEntity> findAll();
}

package com.bankapp.model.repository;

import com.bankapp.model.entity.TransactionEntity;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    void setConnection(java.sql.Connection connection);
    Optional<TransactionEntity> findById(String transactionId);
    List<TransactionEntity> findByAccountId(String accountId);
    List<TransactionEntity> findByCustomerId(String customerId);
    TransactionEntity save(TransactionEntity transaction);
    void delete(String transactionId);
    List<TransactionEntity> findAll();
}

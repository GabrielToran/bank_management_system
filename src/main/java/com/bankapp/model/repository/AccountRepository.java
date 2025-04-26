package com.bankapp.model.repository;

import com.bankapp.model.entity.AccountEntity;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    void setConnection(Connection connection);
    Optional<AccountEntity> findById(String accountId);
    List<AccountEntity> findByCustomerId(String customerId);
    AccountEntity save(AccountEntity account);
    void delete(String accountId);
    List<AccountEntity> findAll();
}

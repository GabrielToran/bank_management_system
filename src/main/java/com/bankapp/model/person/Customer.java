package com.bankapp.model.person;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.bankapp.model.account.Account;

public class Customer extends Person {
    private String customerId;
    private String username;
    private String password;
    private List<Account> accounts;
    public Customer(String id, String firstName, String lastName, String email, String phone,
                    LocalDate dateOfBirth, String address, String customerId, String username, String password) {
        super(id, firstName, lastName, email, phone, dateOfBirth, address);
        this.customerId = customerId;
        this.username = username;
        this.password = password;
        this.accounts = new ArrayList<>();
    }
    // Getters and setters
    public String getCustomerId() {
        return customerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean validatePassword(String passwordAttempt) {
        // In production, use proper password hashing
        return this.password.equals(passwordAttempt);
    }

    public void setPassword(String password) {
        // In production, use proper password hashing
        this.password = password;
    }

    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);  // Return a copy to protect encapsulation
    }
    public void addAccount(Account account) {
        accounts.add(account);
    }

    public boolean removeAccount(String accountId) {
        return accounts.removeIf(account -> account.getAccountId().equals(accountId));
    }
    public Account findAccount(String accountId) {
        return accounts.stream()
                .filter(account -> account.getAccountId().equals(accountId))
                .findFirst()
                .orElse(null);
    }


}

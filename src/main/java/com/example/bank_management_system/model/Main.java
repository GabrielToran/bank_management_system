package com.example.bank_management_system.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;



    // Main application class
    public class Main {
        public static void main(String[] args) {
            BankService bankService = new BankService();

            // Configure integrations
            PaymentGatewayConnector paymentGateway = new PaymentGatewayConnector();
            paymentGateway.configureGateway("sample-api-key", "merchant-12345", true);

            AIChatbotConnector chatbot = new AIChatbotConnector();
            chatbot.configure("https://api.yourchatbot.com/v1", "chatbot-api-key");

            // Example usage
            try {
                // Create customer
                Customer john = bankService.createCustomer("John", "Doe", "john@example.com", "123-456-7890");
                System.out.println("Created: " + john);

                // Create accounts
                Account checkingAccount = bankService.createAccount(john.getCustomerId(), Account.AccountType.CHECKING);
                Account savingsAccount = bankService.createAccount(john.getCustomerId(), Account.AccountType.SAVINGS);

                System.out.println("Created accounts:");
                System.out.println(checkingAccount);
                System.out.println(savingsAccount);

                // Make deposits
                bankService.processDeposit(checkingAccount.getAccountNumber(), new BigDecimal("1000.00"), "Initial deposit");
                bankService.processDeposit(savingsAccount.getAccountNumber(), new BigDecimal("5000.00"), "Initial savings");

                // Check balance
                System.out.println("Checking balance: $" + bankService.getAccountBalance(checkingAccount.getAccountNumber()));
                System.out.println("Savings balance: $" + bankService.getAccountBalance(savingsAccount.getAccountNumber()));

                // Make a transfer
                bankService.transferFunds(checkingAccount.getAccountNumber(), savingsAccount.getAccountNumber(),
                        new BigDecimal("250.00"), "Monthly savings transfer");

                // Make a withdrawal
                bankService.processWithdrawal(checkingAccount.getAccountNumber(), new BigDecimal("120.50"), "ATM withdrawal");

                // Process a payment
                boolean paymentResult = bankService.processPayment(checkingAccount.getAccountNumber(),
                        "utility-company-1", new BigDecimal("75.40"), "Electric bill");
                System.out.println("Payment processed: " + paymentResult);

                // Check updated balances
                System.out.println("Updated checking balance: $" + bankService.getAccountBalance(checkingAccount.getAccountNumber()));
                System.out.println("Updated savings balance: $" + bankService.getAccountBalance(savingsAccount.getAccountNumber()));

                // Test AI chatbot integration
                String chatResponse = bankService.processChatbotQuery(john.getCustomerId(), "What's my current balance?");
                System.out.println("Chatbot response: " + chatResponse);

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }



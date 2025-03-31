package com.example.bank_management_system.model;

import java.util.List;

// AI Chatbot Integration
class AIChatbotConnector {
    private String apiEndpoint;
    private String apiKey;

    public AIChatbotConnector() {
        // Default initialization
        this.apiEndpoint = "https://api.aichatbot.example/v1/chat";
    }

    public void configure(String apiEndpoint, String apiKey) {
        this.apiEndpoint = apiEndpoint;
        this.apiKey = apiKey;
        System.out.println("AI Chatbot configured with endpoint: " + apiEndpoint);
    }

    public String processQuery(Customer customer, List<Account> accounts, String query) {
        // In a real implementation, this would make API calls to an AI chatbot service
        // Here we'll just simulate a response

        System.out.println("Processing query from " + customer.getFirstName() + ": " + query);

        // Simple rule-based responses for demonstration
        if (query.toLowerCase().contains("balance")) {
            return "Your account balances are: " + formatAccountBalances(accounts);
        } else if (query.toLowerCase().contains("transaction")) {
            return "I can help you view recent transactions. Please specify which account.";
        } else {
            return "I'm your banking assistant. I can help with balance inquiries, transactions, and other banking needs.";
        }
    }

    private String formatAccountBalances(List<Account> accounts) {
        StringBuilder result = new StringBuilder();
        for (Account account : accounts) {
            result.append(account.getType()).append(": $").append(account.getBalance()).append("\n");
        }
        return result.toString();
    }
}

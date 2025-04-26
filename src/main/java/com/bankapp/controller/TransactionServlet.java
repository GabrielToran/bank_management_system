package com.bankapp.controller;

import com.bankapp.model.entity.TransactionEntity;
import com.bankapp.model.repository.JpaTransactionRepository;
import com.bankapp.service.TransactionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/transactions")
public class TransactionServlet extends HttpServlet {
    private TransactionService transactionService;

    @Override
    public void init() {
        JpaTransactionRepository transactionRepository = new JpaTransactionRepository();
        transactionService = new TransactionService(transactionRepository);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String accountId = request.getParameter("accountId");
        String customerId = request.getParameter("customerId");
        
        List<TransactionEntity> transactions;
        if (accountId != null) {
            transactions = transactionService.getTransactionsByAccount(accountId);
        } else if (customerId != null) {
            transactions = transactionService.getTransactionsByCustomer(customerId);
        } else {
            transactions = transactionService.getAllTransactions();
        }
        
        request.setAttribute("transactions", transactions);
        request.getRequestDispatcher("/WEB-INF/views/transactions.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Create new transaction logic here
        // Use transactionService.createTransaction()
    }

    @Override
    public void destroy() {
        if (transactionService != null) {
            // Close repository if needed
        }
    }
}
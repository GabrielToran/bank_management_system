package com.bankapp.controller;
import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.bankapp.model.account.Account;
import com.bankapp.model.exception.AccountNotFoundException;
import com.bankapp.model.exception.InsufficientFundsException;
import com.bankapp.model.repository.AccountRepository;
import com.bankapp.model.repository.TransactionRepository;
import com.bankapp.model.transaction.Transaction;
import com.bankapp.service.AccountService;

@WebServlet("/account/*")
public class AccountServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private AccountService accountService;

    @Override
    public void init() throws ServletException {
        AccountRepository accountRepository = new AccountRepository();
        TransactionRepository transactionRepository = new TransactionRepository();
        accountService = new AccountService(accountRepository, transactionRepository);
    }

    /**
     * Display account details
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("customerId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // No specific account, redirect to dashboard
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }

            String accountId = pathInfo.substring(1);  // Remove leading slash
            Account account = accountService.getAccount(accountId);
            List<Transaction> transactions = accountService.getTransactions(accountId);

            request.setAttribute("account", account);
            request.setAttribute("transactions", transactions);
            request.getRequestDispatcher("/account.jsp").forward(request, response);

        } catch (AccountNotFoundException e) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (Exception e) {
            request.setAttribute("error", "Error loading account: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Process account operations
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("customerId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String username = (String) session.getAttribute("username");
        String operation = request.getParameter("operation");
        String accountId = request.getParameter("accountId");

        try {
            switch (operation) {
                case "deposit":
                    handleDeposit(request, username, accountId);
                    break;

                case "withdraw":
                    handleWithdraw(request, username, accountId);
                    break;

                case "transfer":
                    handleTransfer(request, username, accountId);
                    break;

                default:
                    request.setAttribute("error", "Unknown operation: " + operation);
            }

        } catch (InsufficientFundsException e) {
            request.setAttribute("error", "Insufficient funds: " + e.getMessage());
        } catch (Exception e) {
            request.setAttribute("error", "Operation failed: " + e.getMessage());
        }

        // Redirect back to account page
        response.sendRedirect(request.getContextPath() + "/account/" + accountId);
    }

    /**
     * Handle deposit operation
     */
    private void handleDeposit(HttpServletRequest request, String username, String accountId) {
        double amount = Double.parseDouble(request.getParameter("amount"));
        String description = request.getParameter("description");

        if (description == null || description.trim().isEmpty()) {
            description = "Deposit";
        }

        accountService.deposit(accountId, amount, description, username);
        request.setAttribute("success", "Deposit successful");
    }

    /**
     * Handle withdrawal operation
     */
    private void handleWithdraw(HttpServletRequest request, String username, String accountId) {
        double amount = Double.parseDouble(request.getParameter("amount"));
        String description = request.getParameter("description");

        if (description == null || description.trim().isEmpty()) {
            description = "Withdrawal";
        }

        accountService.withdraw(accountId, amount, description, username);
        request.setAttribute("success", "Withdrawal successful");
    }

    /**
     * Handle transfer operation
     */
    private void handleTransfer(HttpServletRequest request, String username, String fromAccountId) {
        double amount = Double.parseDouble(request.getParameter("amount"));
        String toAccountId = request.getParameter("toAccountId");
        String description = request.getParameter("description");

        if (description == null || description.trim().isEmpty()) {
            description = "Transfer";
        }

        accountService.transfer(fromAccountId, toAccountId, amount, description, username);
        request.setAttribute("success", "Transfer successful");
    }
}

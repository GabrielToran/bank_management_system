package com.example.bank_management_system.controller;

import com.example.bank_management_system.model.Account;
import com.example.bank_management_system.model.BankService;
import com.example.bank_management_system.model.Customer;
import com.example.bank_management_system.model.Transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.stream.Collectors;

@WebServlet("/dashboard")
public class BankingServlet extends HttpServlet {
    private final BankService bankService;

    public BankingServlet() {
        this.bankService = new BankService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if (action == null) {
            action = "dashboard";
        }

        switch (action) {
            case "dashboard":
                showDashboard(request, response);
                break;
            case "logout":
                handleLogout(request, response);
                break;
            case "accounts":
                showAccounts(request, response);
                break;
            case "transactions":
                showTransactions(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action parameter is required");
            return;
        }

        switch (action) {
            case "login":
                handleLogin(request, response);
                break;
            case "createCustomer":
                createCustomer(request, response);
                break;
            case "createAccount":
                createAccount(request, response);
                break;
            case "deposit":
                processDeposit(request, response);
                break;
            case "withdraw":
                processWithdrawal(request, response);
                break;
            case "transfer":
                processTransfer(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String customerId = request.getParameter("customerId");
        String password = request.getParameter("password");

        if (customerId == null || password == null ||
                customerId.trim().isEmpty() || password.trim().isEmpty()) {
            request.setAttribute("error", "Please enter both customer ID and password");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        try {
            if (bankService.validateCustomer(customerId, password)) {
                HttpSession session = request.getSession();
                session.setAttribute("customerId", customerId);
                response.sendRedirect(request.getContextPath() + "/dashboard?action=dashboard");
            } else {
                request.setAttribute("error", "Invalid credentials");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", "Login failed: " + e.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!validateSession(session, response)) {
            return;
        }

        String customerId = (String) session.getAttribute("customerId");
        try {
            List<Account> accounts = getCustomerAccounts(customerId);
            request.setAttribute("accounts", accounts);
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }




    private void showAccounts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!validateSession(session, response)) {
            return;
        }

        String customerId = (String) session.getAttribute("customerId");
        try {
            List<Account> accounts = getCustomerAccounts(customerId);
            request.setAttribute("accounts", accounts);
            request.getRequestDispatcher("/WEB-INF/views/accounts.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void showTransactions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!validateSession(session, response)) {
            return;
        }

        String accountNumber = request.getParameter("accountNumber");
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Account number is required");
            return;
        }

        try {
            List<Transaction> transactions = bankService.getAccountTransactions(accountNumber);
            request.setAttribute("transactions", transactions);
            request.getRequestDispatcher("/WEB-INF/views/transactions.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void createCustomer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String phoneNumber = request.getParameter("phoneNumber");

        if (firstName == null || lastName == null || email == null || phoneNumber == null ||
                firstName.trim().isEmpty() || lastName.trim().isEmpty() ||
                email.trim().isEmpty() || phoneNumber.trim().isEmpty()) {
            request.setAttribute("error", "All fields are required");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        try {
            Customer customer = bankService.createCustomer(firstName, lastName, email, phoneNumber);
            HttpSession session = request.getSession();
            session.setAttribute("customerId", customer.getCustomerId());
            response.sendRedirect(request.getContextPath() + "/banking?action=dashboard");
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }

    private void createAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!validateSession(session, response)) {
            return;
        }

        String customerId = (String) session.getAttribute("customerId");
        String accountType = request.getParameter("accountType");

        if (accountType == null || accountType.trim().isEmpty()) {
            request.setAttribute("error", "Account type is required");
            request.getRequestDispatcher("/WEB-INF/views/create-account.jsp").forward(request, response);
            return;
        }

        try {
            Account.AccountType type = Account.AccountType.valueOf(accountType.toUpperCase());
            Account account = bankService.createAccount(customerId, type);
            response.sendRedirect(request.getContextPath() + "/banking?action=accounts");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", "Invalid account type");
            request.getRequestDispatcher("/WEB-INF/views/create-account.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/create-account.jsp").forward(request, response);
        }
    }

    private void processDeposit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!validateSession(session, response)) {
            return;
        }

        String accountNumber = request.getParameter("accountNumber");
        String amountStr = request.getParameter("amount");
        String description = request.getParameter("description");

        try {
            validateAmount(amountStr);
            validateAccountNumber(accountNumber);
            BigDecimal amount = new BigDecimal(amountStr);
            bankService.processDeposit(accountNumber, amount, description);
            response.sendRedirect(request.getContextPath() + "/banking?action=accounts");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/deposit.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/deposit.jsp").forward(request, response);
        }
    }

    private void processWithdrawal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!validateSession(session, response)) {
            return;
        }

        String accountNumber = request.getParameter("accountNumber");
        String amountStr = request.getParameter("amount");
        String description = request.getParameter("description");

        try {
            validateAmount(amountStr);
            validateAccountNumber(accountNumber);
            BigDecimal amount = new BigDecimal(amountStr);
            bankService.processWithdrawal(accountNumber, amount, description);
            response.sendRedirect(request.getContextPath() + "/banking?action=accounts");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/withdraw.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/withdraw.jsp").forward(request, response);
        }
    }

    private void processTransfer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!validateSession(session, response)) {
            return;
        }

        String fromAccountNumber = request.getParameter("fromAccountNumber");
        String toAccountNumber = request.getParameter("toAccountNumber");
        String amountStr = request.getParameter("amount");
        String description = request.getParameter("description");

        try {
            validateAmount(amountStr);
            validateTransferAccounts(fromAccountNumber, toAccountNumber);
            BigDecimal amount = new BigDecimal(amountStr);
            bankService.transferFunds(fromAccountNumber, toAccountNumber, amount, description);
            response.sendRedirect(request.getContextPath() + "/banking?action=accounts");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/transfer.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/transfer.jsp").forward(request, response);
        }
    }

    private List<Account> getCustomerAccounts(String customerId) {
        return bankService.getAllAccounts().stream()
                .filter(account -> account.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }



    private boolean validateSession(HttpSession session, HttpServletResponse response)
            throws IOException {
        if (session == null || session.getAttribute("customerId") == null) {
            HttpServletRequest request = null;
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return false;
        }
        return true;
    }




    private void validateAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount cannot be empty");
        }
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format");
        }
    }

    private void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be empty");
        }
    }

    private void validateTransferAccounts(String fromAccount, String toAccount) {
        validateAccountNumber(fromAccount);
        validateAccountNumber(toAccount);
        if (fromAccount.equals(toAccount)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
    }
}

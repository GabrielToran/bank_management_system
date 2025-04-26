package com.bankapp.controller;
import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.bankapp.service.AccountService;
import com.bankapp.model.entity.AccountEntity;

@WebServlet("/accounts")
public class AccountServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private AccountService accountService;

    @Override
    public void init() {
        JpaAccountRepository accountRepository = new JpaAccountRepository();
        accountService = new AccountService(accountRepository);
    }

    /**
     * Display account details
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String customerId = request.getParameter("customerId");
        
        List<AccountEntity> accounts;
        if (customerId != null) {
            accounts = accountService.getAccountsByCustomer(customerId);
        } else {
            accounts = accountService.getAllAccounts();
        }
        
        request.setAttribute("accounts", accounts);
        request.getRequestDispatcher("/WEB-INF/views/accounts.jsp").forward(request, response);
    }

    /**
     * Process account operations
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Create new account logic here
        // Use accountService.createAccount()
    }

    @Override
    public void destroy() {
        if (accountService != null) {
            // Close repository if needed
        }
    }
}

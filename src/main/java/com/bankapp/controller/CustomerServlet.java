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
import com.bankapp.model.person.Customer;
import com.bankapp.model.repository.CustomerRepository;
import com.bankapp.service.CustomerService;
import com.bankapp.model.repository.AccountRepository;
import com.bankapp.model.repository.TransactionRepository;
import com.bankapp.service.AccountService;

@WebServlet("/dashboard")
public class CustomerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private CustomerService customerService;
    private AccountService accountService;

    @Override
    public void init() throws ServletException {
        CustomerRepository customerRepository = new CustomerRepository();
        AccountRepository accountRepository = new AccountRepository();
        TransactionRepository transactionRepository = new TransactionRepository();

        customerService = new CustomerService(customerRepository, accountRepository, transactionRepository);
        accountService = new AccountService(accountRepository, transactionRepository);
    }

    /**
     * Display dashboard
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("customerId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            String customerId = (String) session.getAttribute("customerId");
            Customer customer = customerService.getCustomer(customerId);
            List<Account> accounts = accountService.getAccountsByCustomer(customerId);

            request.setAttribute("customer", customer);
            request.setAttribute("accounts", accounts);
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            request.setAttribute("error", "Error loading dashboard: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}

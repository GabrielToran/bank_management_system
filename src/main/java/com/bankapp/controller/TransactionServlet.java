package com.bankapp.controller;
import com.bankapp.model.transaction.Transaction;
import com.bankapp.model.repository.TransactionRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.IOException;

@WebServlet("/transactions")
public class TransactionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private TransactionRepository transactionRepository;

    @Override
    public void init() throws ServletException {
        transactionRepository = new TransactionRepository();
    }

    /**
     * Display transactions
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
            String accountId = request.getParameter("accountId");
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");

            List<Transaction> transactions;

            if (accountId != null && !accountId.isEmpty() && startDateStr != null && endDateStr != null) {
                // Filter by date range for specific account
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDateTime startDate = LocalDateTime.parse(startDateStr + "T00:00:00");
                LocalDateTime endDate = LocalDateTime.parse(endDateStr + "T23:59:59");

                transactions = transactionRepository.findByDateRange(accountId, startDate, endDate);
            } else if (accountId != null && !accountId.isEmpty()) {
                // All transactions for specific account
                transactions = transactionRepository.findByAccountId(accountId);
            } else {
                // All transactions for customer
                transactions = transactionRepository.findByCustomerId(customerId);
            }

            request.setAttribute("transactions", transactions);
            request.getRequestDispatcher("/transactions.jsp").forward(request, response);

        } catch (Exception e) {
            request.setAttribute("error", "Error loading transactions: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}
package com.example.banking;
import java.io.IOException;
import java.sql.*;

import com.example.banking.db.DatabaseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/deposit")
public class DepositServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Forward to deposit page
        request.getRequestDispatcher("/dashboard/deposit.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int accountId = (int) session.getAttribute("accountId");

        String amountStr = request.getParameter("amount");
        String description = request.getParameter("description");

        if (amountStr == null || amountStr.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Amount is required");
            request.getRequestDispatcher("/dashboard/deposit.jsp").forward(request, response);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                request.setAttribute("errorMessage", "Amount must be positive");
                request.getRequestDispatcher("/dashboard/deposit.jsp").forward(request, response);
                return;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Invalid amount format");
            request.getRequestDispatcher("/dashboard/deposit.jsp").forward(request, response);
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // Update account balance
            pstmt = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE id = ?");
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();

            DatabaseUtil.closeStatement(pstmt);

            // Record transaction
            pstmt = conn.prepareStatement(
                    "INSERT INTO transactions (account_id, transaction_type, amount, description) " +
                            "VALUES (?, 'DEPOSIT', ?, ?)");
            pstmt.setInt(1, accountId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, description);
            pstmt.executeUpdate();

            conn.commit();

            request.setAttribute("successMessage", "Deposit successful! $" + String.format("%.2f", amount) + " added to your account.");
            request.getRequestDispatcher("/dashboard/deposit.jsp").forward(request, response);

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            request.setAttribute("errorMessage", "Deposit failed: " + e.getMessage());
            request.getRequestDispatcher("/dashboard/deposit.jsp").forward(request, response);
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
    }
}

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

@WebServlet("/transfer")
public class TransferServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Forward to transfer page
        request.getRequestDispatcher("/dashboard/transfer.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int accountId = (int) session.getAttribute("accountId");
        String username = (String) session.getAttribute("username");

        String recipientUsername = request.getParameter("recipient");
        String amountStr = request.getParameter("amount");
        String description = request.getParameter("description");

        if (recipientUsername == null || recipientUsername.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Recipient username is required");
            request.getRequestDispatcher("/dashboard/transfer.jsp").forward(request, response);
            return;
        }

        if (recipientUsername.equals(username)) {
            request.setAttribute("errorMessage", "Cannot transfer to your own account");
            request.getRequestDispatcher("/dashboard/transfer.jsp").forward(request, response);
            return;
        }

        if (amountStr == null || amountStr.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Amount is required");
            request.getRequestDispatcher("/dashboard/transfer.jsp").forward(request, response);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                request.setAttribute("errorMessage", "Amount must be positive");
                request.getRequestDispatcher("/dashboard/transfer.jsp").forward(request, response);
                return;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Invalid amount format");
            request.getRequestDispatcher("/dashboard/transfer.jsp").forward(request, response);
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();

            // Find recipient's account
            pstmt = conn.prepareStatement(
                    "SELECT a.id FROM accounts a " +
                            "JOIN users u ON a.user_id = u.id " +
                            "WHERE u.username = ?");
            pstmt.setString(1, recipientUsername);
            rs = pstmt.executeQuery();

            int recipientAccountId;
            if (rs.next()) {
                recipientAccountId = rs.getInt("id");
            } else {
                request.setAttribute("errorMessage", "Recipient not found");
                request.getRequestDispatcher("/dashboard/transfer.jsp").forward(request, response);
                return;
            }

            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);

            // Check if sufficient balance
            pstmt = conn.prepareStatement("SELECT balance FROM accounts WHERE id = ?");
            pstmt.setInt(1, accountId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                if (balance < amount) {
                    request.setAttribute("errorMessage", "Insufficient balance. Current balance: $" + String.format("%.2f", balance));
                    request.getRequestDispatcher("/dashboard/transfer.jsp").forward(request, response);
                    return;
                }
            }

            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);

            conn.setAutoCommit(false);

            // Deduct from sender's account
            pstmt = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE id = ?");
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();

            DatabaseUtil.closeStatement(pstmt);

            // Add to recipient's account
            pstmt = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE id = ?");
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, recipientAccountId);
            pstmt.executeUpdate();

            DatabaseUtil.closeStatement(pstmt);

            // Record transaction
            pstmt = conn.prepareStatement(
                    "INSERT INTO transactions (account_id, transaction_type, amount, description) " +
                            "VALUES (?, 'TRANSFER', ?, ?)");
            pstmt.setInt(1, accountId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, "Transfer to " + recipientUsername +
                    (description != null && !description.isEmpty() ? ": " + description : ""));
            pstmt.executeUpdate();

            conn.commit();

            request.setAttribute("successMessage", "Transfer successful! $" + String.format("%.2f", amount) +
                    " transferred to " + recipientUsername);
            request.getRequestDispatcher("/dashboard/transfer.jsp").forward(request, response);

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            request.setAttribute("errorMessage", "Transfer failed: " + e.getMessage());
            request.getRequestDispatcher("/dashboard/transfer.jsp").forward(request, response);
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
    }
}

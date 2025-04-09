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

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int accountId = (int) session.getAttribute("accountId");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();

            // Get account balance
            pstmt = conn.prepareStatement("SELECT balance FROM accounts WHERE id = ?");
            pstmt.setInt(1, accountId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                request.setAttribute("balance", balance);
            }

            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);

            // Get recent transactions
            pstmt = conn.prepareStatement(
                    "SELECT transaction_type, amount, description, created_at " +
                            "FROM transactions " +
                            "WHERE account_id = ? " +
                            "ORDER BY created_at DESC LIMIT 5");
            pstmt.setInt(1, accountId);

            rs = pstmt.executeQuery();

            request.setAttribute("transactions", rs);

            // Forward to dashboard page
            request.getRequestDispatcher("/dashboard/home.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Failed to load dashboard: " + e.getMessage());
            request.getRequestDispatcher("/dashboard/home.jsp").forward(request, response);
        } finally {
            DatabaseUtil.closeResultSet(rs);
            DatabaseUtil.closeStatement(pstmt);
            DatabaseUtil.closeConnection(conn);
        }
    }
}

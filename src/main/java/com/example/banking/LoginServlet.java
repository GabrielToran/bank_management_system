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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Single user credentials - in a real app, consider using environment variables or a config file
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123"; // Use a strong password in production

    @Override
    public void init() throws ServletException {
        DatabaseUtil.initializeDatabase();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Validate against hardcoded single user credentials
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            // Create session for the single user
            HttpSession session = request.getSession();
            session.setAttribute("loggedIn", true);
            session.setAttribute("username", ADMIN_USERNAME);

            // Get the single account information from the database
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;

            try {
                conn = DatabaseUtil.getConnection();
                stmt = conn.createStatement();
                rs = stmt.executeQuery("SELECT id, account_number FROM account LIMIT 1");

                if (rs.next()) {
                    session.setAttribute("accountId", rs.getLong("id"));
                    session.setAttribute("accountNumber", rs.getString("account_number"));
                }

                // Redirect to dashboard
                response.sendRedirect("dashboard/home.jsp");
            } catch (SQLException e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Database error: " + e.getMessage());
                request.getRequestDispatcher("login.html").forward(request, response);
            } finally {
                DatabaseUtil.closeResultSet(rs);
                DatabaseUtil.closeStatement(stmt);
                DatabaseUtil.closeConnection(conn);
            }
        } else {
            // Invalid credentials
            request.setAttribute("errorMessage", "Invalid username or password");
            request.getRequestDispatcher("login.html").forward(request, response);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Forward to login page
        request.getRequestDispatcher("home.html").forward(request, response);
    }
}
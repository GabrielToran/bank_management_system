<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.DecimalFormat" %>

<%
    // Check if user is logged in
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    String fullname = (String) session.getAttribute("fullname");
    DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Banking System</title>
    <link rel="stylesheet" href="../css/styles.css">
</head>
<body>
<div class="dashboard-container">
    <!-- Include the navigation sidebar -->
    <jsp:include page="sidebar.jsp" />

    <div class="dashboard-content">
        <header class="dashboard-header">
            <h1>Dashboard</h1>
            <div class="user-info">
                Welcome, <%= fullname %>
            </div>
        </header>

        <div class="dashboard-main">
            <!-- Balance Card -->
            <div class="card balance-card">
                <h2>Account Balance</h2>
                <div class="balance-amount">
                    <%
                        Double balance = (Double) request.getAttribute("balance");
                        if (balance != null) {
                            out.print(currencyFormat.format(balance));
                        } else {
                            out.print("$0.00");
                        }
                    %>
                </div>
                <div class="balance-actions">
                    <a href="../deposit" class="btn btn-secondary">Deposit</a>
                    <a href="../withdraw" class="btn btn-outline">Withdraw</a>
                    <a href="../transfer" class="btn btn-outline">Transfer</a>
                </div>
            </div>

            <!-- Error message if any -->
            <% if (request.getAttribute("errorMessage") != null) { %>
            <div class="alert alert-error">
                <%= request.getAttribute("errorMessage") %>
            </div>
            <% } %>

            <!-- Recent Transactions -->
            <div class="card transactions-card">
                <h2>Recent Transactions</h2>
                <table class="transactions-table">
                    <thead>
                    <tr>
                        <th>Type</th>
                        <th>Amount</th>
                        <th>Description</th>
                        <th>Date</th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        ResultSet transactions = (ResultSet) request.getAttribute("transactions");
                        if (transactions != null) {
                            while (transactions.next()) {
                                String type = transactions.getString("transaction_type");
                                double amount = transactions.getDouble("amount");
                                String description = transactions.getString("description");
                                java.sql.Timestamp date = transactions.getTimestamp("created_at");

                                String typeClass = "";
                                if (type.equals("DEPOSIT")) {
                                    typeClass = "deposit";
                                } else if (type.equals("WITHDRAWAL")) {
                                    typeClass = "withdrawal";
                                } else {
                                    typeClass = "transfer";
                                }
                    %>
                    <tr>
                        <td><span class="transaction-type <%= typeClass %>"><%= type %></span></td>
                        <td class="amount <%= typeClass %>">
                            <%= type.equals("WITHDRAWAL") || type.equals("TRANSFER") ? "-" : "" %><%= currencyFormat.format(amount) %>
                        </td>
                        <td><%= description != null ? description : "" %></td>
                        <td><%= dateFormat.format(date) %></td>
                    </tr>
                    <%
                        }
                    } else {
                    %>
                    <tr>
                        <td colspan="4" class="no-data">No recent transactions</td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
                <div class="view-all">
                    <a href="../history">View All Transactions</a>
                </div>
            </div>

            <!-- Quick Actions -->
            <div class="card quick-actions-card">
                <h2>Quick Actions</h2>
                <div class="quick-actions">
                    <a href="../deposit" class="quick-action">
                        <div class="action-icon deposit-icon">💰</div>
                        <div class="action-name">Deposit</div>
                    </a>
                    <a href="../withdraw" class="quick-action">
                        <div class="action-icon withdraw-icon">💸</div>
                        <div class="action-name">Withdraw</div>
                    </a>
                    <a href="../transfer" class="quick-action">
                        <div class="action-icon transfer-icon">🔄</div>
                        <div class="action-name">Transfer</div>
                    </a>
                    <a href="../profile" class="quick-action">
                        <div class="action-icon profile-icon">👤</div>
                        <div class="action-name">Profile</div>
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
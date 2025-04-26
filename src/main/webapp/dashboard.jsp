<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.bankapp.model.person.Customer" %>
<%@ page import="com.bankapp.model.account.Account" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - SecureBank</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<%
    // Check if user is logged in
    if (session.getAttribute("customer") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    Customer customer = (Customer) session.getAttribute("customer");
    List<Account> accounts = (List<Account>) request.getAttribute("accounts");
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
%>

<div class="container">
    <header class="dashboard-header">
        <h1>SecureBank</h1>
        <nav>
            <ul>
                <li><a href="dashboard.jsp" class="active">Dashboard</a></li>
                <li><a href="account.jsp">Accounts</a></li>
                <li><a href="transaction.jsp">Transactions</a></li>
                <li><a href="LoginServlet?action=logout">Logout</a></li>
            </ul>
        </nav>
    </header>

    <main class="dashboard-main">
        <section class="welcome-section">
            <h2>Welcome, <%= customer.getFirstName() %>!</h2>
            <p class="last-login">Last login: <%= session.getAttribute("lastLoginTime") != null ? session.getAttribute("lastLoginTime") : "First login" %></p>
        </section>

        <section class="account-summary">
            <h3>Account Summary</h3>

            <div class="account-cards">
                <% if (accounts != null && !accounts.isEmpty()) {
                    for (Account account : accounts) { %>
                <div class="account-card">
                    <div class="account-info">
                        <h4><%= account.getAccountType() %></h4>
                        <p class="account-number">**** <%= account.getAccountNumber().substring(account.getAccountNumber().length() - 4) %></p>
                    </div>
                    <div class="account-balance">
                        <p class="balance-amount"><%= currencyFormatter.format(account.getBalance()) %></p>
                        <a href="AccountServlet?action=view&id=<%= account.getId() %>" class="btn secondary small">Details</a>
                    </div>
                </div>
                <% }
                } else { %>
                <div class="no-accounts">
                    <p>You don't have any accounts yet.</p>
                    <a href="AccountServlet?action=create" class="btn primary">Open an Account</a>
                </div>
                <% } %>
            </div>
        </section>

        <section class="quick-actions">
            <h3>Quick Actions</h3>
            <div class="action-buttons">
                <a href="TransactionServlet?action=transfer" class="action-btn">
                    <span class="action-icon">↗</span>
                    <span class="action-text">Transfer</span>
                </a>
                <a href="TransactionServlet?action=deposit" class="action-btn">
                    <span class="action-icon">↓</span>
                    <span class="action-text">Deposit</span>
                </a>
                <a href="TransactionServlet?action=withdraw" class="action-btn">
                    <span class="action-icon">↑</span>
                    <span class="action-text">Withdraw</span>
                </a>
                <a href="TransactionServlet?action=history" class="action-btn">
                    <span class="action-icon">≡</span>
                    <span class="action-text">History</span>
                </a>
            </div>
        </section>

        <section class="recent-transactions">
            <h3>Recent Transactions</h3>
            <%
                List<?> recentTransactions = (List<?>) request.getAttribute("recentTransactions");
                if (recentTransactions != null && !recentTransactions.isEmpty()) {
            %>
            <table class="transactions-table">
                <thead>
                <tr>
                    <th>Date</th>
                    <th>Description</th>
                    <th>Amount</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
                <!-- Transaction data would be populated here -->
                <tr>
                    <td>04/21/2025</td>
                    <td>Direct Deposit</td>
                    <td class="amount positive">$1,250.00</td>
                    <td><span class="status completed">Completed</span></td>
                </tr>
                <tr>
                    <td>04/19/2025</td>
                    <td>Grocery Store</td>
                    <td class="amount negative">-$87.33</td>
                    <td><span class="status completed">Completed</span></td>
                </tr>
                </tbody>
            </table>
            <a href="TransactionServlet?action=history" class="view-all">View All Transactions</a>
            <% } else { %>
            <p class="no-transactions">No recent transactions.</p>
            <% } %>
        </section>
    </main>

    <footer>
        <p>&copy; 2025 SecureBank. All rights reserved.</p>
        <div class="footer-links">
            <a href="#">Privacy Policy</a>
            <a href="#">Terms of Service</a>
            <a href="#">Security</a>
        </div>
    </footer>
</div>

<script src="js/script.js"></script>
</body>
</html>
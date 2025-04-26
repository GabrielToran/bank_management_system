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
    <title>Accounts - SecureBank</title>
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
    Account selectedAccount = (Account) request.getAttribute("selectedAccount");
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    String action = request.getParameter("action") != null ? request.getParameter("action") : "list";
%>

<div class="container">
    <header class="dashboard-header">
        <h1>SecureBank</h1>
        <nav>
            <ul>
                <li><a href="dashboard.jsp">Dashboard</a></li>
                <li><a href="account.jsp" class="active">Accounts</a></li>
                <li><a href="transaction.jsp">Transactions</a></li>
                <li><a href="LoginServlet?action=logout">Logout</a></li>
            </ul>
        </nav>
    </header>

    <main class="account-main">
        <% if (action.equals("view") && selectedAccount != null) { %>
        <!-- Account Details View -->
        <section class="account-details">
            <div class="back-link">
                <a href="account.jsp">&larr; Back to Accounts</a>
            </div>

            <div class="account-header">
                <h2><%= selectedAccount.getAccountType() %> Account</h2>
                <p class="account-number">Account #: <%= selectedAccount.getAccountNumber() %></p>
            </div>

            <div class="account-balance-card">
                <div class="balance-info">
                    <span class="balance-label">Available Balance</span>
                    <span class="balance-value"><%= currencyFormatter.format(selectedAccount.getBalance()) %></span>
                </div>

                <div class="account-actions">
                    <a href="TransactionServlet?action=deposit&accountId=<%= selectedAccount.getId() %>" class="btn primary">Deposit</a>
                    <a href="TransactionServlet?action=withdraw&accountId=<%= selectedAccount.getId() %>" class="btn primary">Withdraw</a>
                    <a href="TransactionServlet?action=transfer&fromAccountId=<%= selectedAccount.getId() %>" class="btn secondary">Transfer</a>
                </div>
            </div>

            <div class="account-details-info">
                <h3>Account Information</h3>
                <div class="details-grid">
                    <div class="detail-item">
                        <span class="detail-label">Account Type</span>
                        <span class="detail-value"><%= selectedAccount.getAccountType() %></span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Account Number</span>
                        <span class="detail-value"><%= selectedAccount.getAccountNumber() %></span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Opening Date</span>
                        <span class="detail-value"><%= selectedAccount.getOpeningDate() %></span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Current Balance</span>
                        <span class="detail-value"><%= currencyFormatter.format(selectedAccount.getBalance()) %></span>
                    </div>
                    <% if (selectedAccount.getAccountType().equals("Savings")) { %>
                    <div class="detail-item">
                        <span class="detail-label">Interest Rate</span>
                        <span class="detail-value">2.25%</span>
                    </div>
                    <% } else if (selectedAccount.getAccountType().equals("Loan")) { %>
                    <div class="detail-item">
                        <span class="detail-label">Interest Rate</span>
                        <span class="detail-value">5.75%</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Payment Due</span>
                        <span class="detail-value">05/15/2025</span>
                    </div>
                    <% } %>
                </div>
            </div>

            <div class="account-transactions">
                <h3>Recent Transactions</h3>
                <table class="transactions-table">
                    <thead>
                    <tr>
                        <th>Date</th>
                        <th>Description</th>
                        <th>Amount</th>
                        <th>Balance</th>
                    </tr>
                    </thead>
                    <tbody>
                    <!-- Transaction data would be populated here -->
                    <tr>
                        <td>04/21/2025</td>
                        <td>Direct Deposit</td>
                        <td class="amount positive">$1,250.00</td>
                        <td>$3,427.81</td>
                    </tr>
                    <tr>
                        <td>04/19/2025</td>
                        <td>ATM Withdrawal</td>
                        <td class="amount negative">-$200.00</td>
                        <td>$2,177.81</td>
                    </tr>
                    </tbody>
                </table>
                <a href="TransactionServlet?action=history&accountId=<%= selectedAccount.getId() %>" class="view-all">View All Transactions</a>
            </div>
        </section>
        <% } else if (action.equals("create")) { %>
        <!-- Create Account Form -->
        <section class="create-account">
            <h2>Open a New Account</h2>

            <form action="AccountServlet" method="post" class="account-form">
                <input type="hidden" name="action" value="create">

                <div class="form-group">
                    <label for="accountType">Account Type</label>
                    <select id="accountType" name="accountType" required>
                        <option value="">-- Select Account Type --</option>
                        <option value="Savings">Savings Account</option>
                        <option value="Checking">Checking Account</option>
                        <option value="Loan">Loan Account</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="initialDeposit">Initial Deposit Amount</label>
                    <input type="number" id="initialDeposit" name="initialDeposit" min="0" step="0.01" required>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn primary">Open Account</button>
                    <a href="account.jsp" class="btn secondary">Cancel</a>
                </div>
            </form>
        </section>
        <% } else { %>
        <!-- Account List View -->
        <section class="accounts-list">
            <div class="section-header">
                <h2>Your Accounts</h2>
                <a href="AccountServlet?action=create" class="btn primary">Open New Account</a>
            </div>

            <div class="account-cards-container">
                <% if (accounts != null && !accounts.isEmpty()) {
                    for (Account account : accounts) { %>
                <div class="account-card">
                    <div class="account-type-icon <%= account.getAccountType().toLowerCase() %>">
                        <%= account.getAccountType().charAt(0) %>
                    </div>
                    <div class="account-details">
                        <h3><%= account.getAccountType() %> Account</h3>
                        <p class="account-number"><%= account.getAccountNumber() %></p>
                        <p class="balance"><%= currencyFormatter.format(account.getBalance()) %></p>
                    </div>
                    <div class="account-actions">
                        <a href="AccountServlet?action=view&id=<%= account.getAccountId() %>" class="btn secondary">View Details</a>
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
        <% } %>
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
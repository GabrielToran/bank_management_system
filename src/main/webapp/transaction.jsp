<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.bankapp.model.person.Customer" %>
<%@ page import="com.bankapp.model.account.Account" %>
<%@ page import="com.bankapp.model.transaction.Transaction" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Transactions - SecureBank</title>
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
    List<Transaction> transactions = (List<Transaction>) request.getAttribute("transactions");
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    String action = request.getParameter("action") != null ? request.getParameter("action") : "history";
    %>

<div class="container">
    <header class="dashboard-header">
        <h1>SecureBank</h1>
        <nav>
            <ul>
                <li><a href="dashboard.jsp">Dashboard</a></li>
                <li><a href="account.jsp">Accounts</a></li>
                <li><a href="transaction.jsp" class="active">Transactions</a></li>
                <li><a href="LoginServlet?action=logout">Logout</a></li>
            </ul>
        </nav>
    </header>

    <main class="transaction-main">
            <% if (action.equals("deposit")) { %>
        <!-- Deposit Form -->
        <section class="transaction-form-section">
            <h2>Make a Deposit</h2>

            <form action="TransactionServlet" method="post" class="transaction-form">
                <input type="hidden" name="action" value="deposit">

                <div class="form-group">
                    <label for="accountId">Select Account</label>
                    <select id="accountId" name="accountId" required>
                        <option value="">-- Select Account --</option>
                        <% if (accounts != null) {
                            for (Account account : accounts) { %>
                        <option value="<%= account.getId() %>" <%= request.getParameter("accountId") != null && request.getParameter("accountId").equals(String.valueOf(account.getId())) ? "selected" : "" %>>
                            <%= account.getAccountType() %> (<%= account.getAccountNumber() %>) - <%= currencyFormatter.format(account.getBalance()) %>
                        </option>
                        <% }
                        } %>
                    </select>
                </div>

                <div class="form-group">
                    <label for="amount">Amount</label>
                    <div class="amount-input">
                        <span class="currency-symbol">$</span>
                        <input type="number" id="amount" name="amount" min="0.01" step="0.01" required>
                    </div>
                </div>

                <div class="form-group">
                    <label for="description">Description (Optional)</label>
                    <input type="text" id="description" name="description" placeholder="e.g., Paycheck deposit">
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn primary">Complete Deposit</button>
                    <a href="transaction.jsp" class="btn secondary">Cancel</a>
                </div>
            </form>
        </section>
            <% } else if (action.equals("withdraw")) { %>
        <!-- Withdraw Form -->
        <section class="transaction-form-section">
            <h2>Make a Withdrawal</h2>

            <form action="TransactionServlet" method="post" class="transaction-form">
                <input type="hidden" name="action" value="withdraw">

                <div class="form-group">
                    <label for="accountId">Select Account</label>
                    <select id="accountId" name="accountId" required>
                        <option value="">-- Select Account --</option>
                        <% if (accounts != null) {
                            for (Account account : accounts) { %>
                        <option value="<%= account.getId() %>" <%= request.getParameter("accountId") != null && request.getParameter("accountId").equals(String.valueOf(account.getId())) ? "selected" : "" %>>
                            <%= account.getAccountType() %> (<%= account.getAccountNumber() %>) - <%= currencyFormatter.format(account.getBalance()) %>
                        </option>
                        <% }
                        } %>
                    </select>
                </div>

                <div class="form-group">
                    <label for="amount">Amount</label>
                    <div class="amount-input">
                        <span class="currency-symbol">$</span>
                        <input type="number" id="amount" name="amount" min="0.01" step="0.01" required>
                    </div>
                </div>

                <div class="form-group">
                    <label for="description">Description (Optional)</label>
                    <input type="text" id="description" name="description" placeholder="e.g., ATM withdrawal">
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn primary">Complete Withdrawal</button>
                    <a href="transaction.jsp" class="btn secondary">Cancel</a>
                </div>
            </form>
        </section>
            <% } else if (action.equals("transfer")) { %>
        <!-- Transfer Form -->
        <section class="transaction-form-section">
            <h2>Transfer Funds</h2>

            <form action="TransactionServlet" method="post" class="transaction-form">
                <input type="hidden" name="action" value="transfer">

                <div class="form-group">
                    <label for="fromAccountId">From Account</label>
                    <select id="fromAccountId" name="fromAccountId" required>
                        <option value="">-- Select Source Account --</option>
                        <% if (accounts != null) {
                            for (Account account : accounts) { %>
                        <option value="<%= account.getId() %>" <%= request.getParameter("fromAccountId") != null && request.getParameter("fromAccountId").equals(String.valueOf(account.getId())) ? "selected" : "" %>>
                            <%= account.getAccountType() %> (<%= account.getAccountNumber() %>) - <%= currencyFormatter.format(account.getBalance()) %>
                        </option>
                        <% }
                        } %>
                    </select>
                </div>

                <div class="form-group">
                    <label for="toAccountId">To Account</label>
                    <select id="toAccountId" name="toAccountId" required>
                        <option value="">-- Select Destination Account --</option>
                        <% if (accounts != null) {
                            for (Account account : accounts) { %>
                        <option value="<%= account.getId() %>">
                            <%= account.getAccountType() %> (<%= account.getAccountNumber() %>) - <%= currencyFormatter.format(account.getBalance()) %>
                        </option>
                        <% }
                        } %>
                    </select>
                </div>

                <div class="form-group">
                    <label for="amount">Amount</label>
                    <div class="amount-input">
                        <span class="currency-symbol">$</span>
                        <input type="number" id="amount" name="amount" min="0.01" step="0.01" required>
                    </div>
                </div>

                <div class="form-group">
                    <label for="description">Description (Optional)</label>
                    <input type="text" id="description" name="description" placeholder="e.g., Rent payment">
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn primary">Complete Transfer</button>
                    <a href="transaction.jsp" class="btn secondary">Cancel</a>
                </div>
            </form>
        </section>
        <% } else { /* Default: Transaction History */ %>
        <!-- Transaction History -->
        <section class="transaction-history">
            <div class="section-header">
                <h2>Transaction History</h2>
                <div class="transaction-actions">
                    <a href="TransactionServlet?action=deposit" class="btn primary">Deposit</a>
                    <a href="TransactionServlet?action=withdraw" class="btn primary">Withdraw</a>
                    <a href="TransactionServlet?action=transfer" class="btn primary">Transfer</a>
                </div>
            </div>

            <div class="filter-container">
                <form action="TransactionServlet" method="get" class="filter-form">
                    <input type="hidden" name="action" value="history">

                    <div class="filter-group">
                        <label for="accountFilter">Account:</label>
                        <select id="accountFilter" name="accountId">
                            <option value="">All Accounts</option>
                            <% if (accounts != null) {
                                for (Account account : accounts) { %>
                            <option value="<%= account.getId() %>" <%= request.getParameter("accountId") != null && request.getParameter("accountId").equals(String.valueOf(account.getId())) ? "selected" : "" %>>
                                <%= account.getAccountType() %> (<%= account.getAccountNumber().substring(account.getAccountNumber().length() - 4) %>)
                            </option>
                            <% }
                            } %>
                        </select>
                    </div>

                    <div class="filter-group">
                        <label for="typeFilter">Type:</label>
                        <select id="typeFilter" name="type">
                            <option value="">All Types</option>
                            <option value="DEPOSIT" <%= "DEPOSIT".equals(request.getParameter("type")) ? "selected" : "" %>>Deposits</option>
                            <option value="WITHDRAWAL" <%= "WITHDRAWAL".equals(request.getParameter("type")) ? "selected" : "" %>>Withdrawals</option>
                            <option value="TRANSFER" <%= "TRANSFER".equals(request.getParameter("type")) ? "selected" : "" %>>Transfers</option>
                        </select>
                    </div>

                    <div class="filter-group">
                        <label for="dateFrom">From:</label>
                        <input type="date" id="dateFrom" name="dateFrom" value="<%= request.getParameter("dateFrom") != null ? request.getParameter("dateFrom") : "" %>">
                    </div>

                    <div class="filter-group">
                        <label for="dateTo">To:</label>
                        <input type="date" id="dateTo" name="dateTo" value="<%= request.getParameter("dateTo") != null ? request.getParameter("dateTo") : "" %>">
                    </div>

                    <div class="filter-actions">
                        <button type="submit" class="btn secondary">Apply Filters</button>
                        <a href="transaction.jsp" class="btn text">Clear Filters</a>
                    </div>
                </form>
            </div>

            <% if (transactions != null && !transactions.isEmpty()) { %>
            <table class="transactions-table">
                <thead>
                <tr>
                    <th>Date</th>
                    <th>Description</th>
                    <th>Account</th>
                    <th>Type</th>
                    <th>Amount</th>
                    <th>Balance</th>
                </tr>
                </thead>
                <tbody>
                <% for (Transaction transaction : transactions) { %>
                <tr>
                    <td><%= transaction.getTransactionDate() %></td>
                    <td><%= transaction.getDescription() %></td>
                    <td><%= transaction.getAccountNumber() %></td>
                    <td><%= transaction.getTransactionType() %></td>
                    <td class="amount <%= transaction.getAmount() >= 0 ? "positive" : "negative" %>">
                        <%= currencyFormatter.format(transaction.getAmount()) %>
                    </td>
                    <td><%= currencyFormatter.format(transaction.getBalanceAfter()) %></td>
                </tr>
                <% } %>
                </tbody>
            </table>

            <div class="pagination">
                <%
                    int currentPage = request.getAttribute("currentPage") != null ? (int) request.getAttribute("currentPage") : 1;
                    int totalPages = request.getAttribute("totalPages") != null ? (int) request.getAttribute("totalPages") : 1;

                    if (totalPages > 1) {
                        String baseUrl = "TransactionServlet?action=history";
                        if (request.getParameter("accountId") != null) baseUrl += "&accountId=" + request.getParameter("accountId");
                        if (request.getParameter("type") != null) baseUrl += "&type=" + request.getParameter("type");
                        if (request.getParameter("dateFrom") != null) baseUrl += "&dateFrom=" + request.getParameter("dateFrom");
                        if (request.getParameter("dateTo") != null) baseUrl += "&dateTo=" + request.getParameter("dateTo");
                %>
                <div class="page-controls">
                    <a href="<%= baseUrl %>&page=1" class="page-control <%= currentPage == 1 ? "disabled" : "" %>">&laquo;</a>
                    <a href="<%= baseUrl %>&page=<%= currentPage - 1 %>" class="page-control <%= currentPage == 1 ? "disabled" : "" %>">&lsaquo;</a>

                    <%
                        int startPage = Math.max(1, currentPage - 2);
                        int endPage = Math.min(totalPages, startPage + 4);
                        if (endPage - startPage < 4) {
                            startPage = Math.max(1, endPage - 4);
                        }

                        for (int i = startPage; i <= endPage; i++) { %>
                    <a href="<%= baseUrl %>&page=<%= i %>" class="page-number <%= i == currentPage ? "active" : "" %>"><%= i %></a>
                    <% } %>

                    <a href="<%= baseUrl %>&page=<%= currentPage + 1 %>" class="page-control <%= currentPage == totalPages ? "disabled" : "" %>">&rsaquo;</a>
                    <a href="<%= baseUrl %>&page=<%= totalPages %>" class="page-control <%= currentPage == totalPages ? "disabled" : "" %>">&raquo;</a>
                </div>
                <div class="page-info">
                    Page <%= currentPage %> of <%= totalPages %>
                </div>
                <% } %>
            </div>
            <% } else { %>
            <div class="no-transactions">
                <p>No transactions found matching your criteria.</p>
            </div>
            <% } %>
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
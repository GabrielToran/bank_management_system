<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Check if user is logged in
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    String fullname = (String) session.getAttribute("fullname");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Withdraw - Banking System</title>
    <link rel="stylesheet" href="../css/styles.css">
</head>
<body>
<div class="dashboard-container">
    <!-- Include the navigation sidebar -->
    <jsp:include page="sidebar.jsp" />

    <div class="dashboard-content">
        <header class="dashboard-header">
            <h1>Withdraw Funds</h1>
            <div class="user-info">
                Welcome, <%= fullname %>
            </div>
        </header>

        <div class="dashboard-main">
            <div class="card">
                <!-- Success message if any -->
                <% if (request.getAttribute("successMessage") != null) { %>
                <div class="alert alert-success">
                    <%= request.getAttribute("successMessage") %>
                </div>
                <% } %>

                <!-- Error message if any -->
                <% if (request.getAttribute("errorMessage") != null) { %>
                <div class="alert alert-error">
                    <%= request.getAttribute("errorMessage") %>
                </div>
                <% } %>

                <form action="../withdraw" method="post" class="form">
                    <div class="form-group">
                        <label for="amount">Amount to Withdraw ($)</label>
                        <input type="number" id="amount" name="amount" step="0.01" min="0.01" required>
                    </div>

                    <div class="form-group">
                        <label for="description">Description (Optional)</label>
                        <input type="text" id="description" name="description" placeholder="e.g., Bills, Shopping">
                    </div>

                    <div class="form-group">
                        <button type="submit" class="btn btn-primary">Withdraw Funds</button>
                        <a href="../dashboard/home.jsp" class="btn btn-outline">Cancel</a>
                    </div>
                </form>
            </div>

            <!-- Withdraw Tips -->
            <div class="card info-card">
                <h3>Withdrawal Information</h3>
                <ul class="info-list">
                    <li>
                        <strong>Available Balance:</strong>
                        You can only withdraw funds up to your available balance.
                    </li>
                    <li>
                        <strong>Instant Processing:</strong>
                        All withdrawals are processed immediately and will reflect in your balance instantly.
                    </li>
                    <li>
                        <strong>Record Keeping:</strong>
                        Adding a description helps you track your expenses.
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>
</body>
</html>
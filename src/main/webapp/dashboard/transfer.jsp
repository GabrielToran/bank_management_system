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
    <title>Transfer - Banking System</title>
    <link rel="stylesheet" href="../css/styles.css">
</head>
<body>
<div class="dashboard-container">
    <!-- Include the navigation sidebar -->
    <jsp:include page="sidebar.jsp" />

    <div class="dashboard-content">
        <header class="dashboard-header">
            <h1>Transfer Funds</h1>
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

                <form action="../transfer" method="post" class="form">
                    <div class="form-group">
                        <label for="recipient">Recipient Username</label>
                        <input type="text" id="recipient" name="recipient" required>
                    </div>

                    <div class="form-group">
                        <label for="amount">Amount to Transfer ($)</label>
                        <input type="number" id="amount" name="amount" step="0.01" min="0.01" required>
                    </div>

                    <div class="form-group">
                        <label for="description">Description (Optional)</label>
                        <input type="text" id="description" name="description" placeholder="e.g., Payment, Gift">
                    </div>

                    <div class="form-group">
                        <button type="submit" class="btn btn-primary">Transfer Funds</button>
                        <a href="../dashboard/home.jsp" class="btn btn-outline">Cancel</a>
                    </div>
                </form>
            </div>

            <!-- Transfer Tips -->
            <div class="card info-card">
                <h3>Transfer Information</h3>
                <ul class="info-list">
                    <li>
                        <strong>Recipient Username:</strong>
                        Enter the exact username of the account holder you wish to transfer funds to.
                    </li>
                    <li>
                        <strong>Available Balance:</strong>
                        You can only transfer funds up to your available balance.
                    </li>
                    <li>
                        <strong>Instant Processing:</strong>
                        All transfers are processed immediately and will reflect in both accounts instantly.
                    </li>
                    <li>
                        <strong>No Fees:</strong>
                        SecureBank doesn't charge any fees for transfers between accounts.
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>
</body>
</html>
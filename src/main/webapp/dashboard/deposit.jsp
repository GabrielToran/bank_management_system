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
  <title>Deposit - Banking System</title>
  <link rel="stylesheet" href="../css/styles.css">
</head>
<body>
<div class="dashboard-container">
  <!-- Include the navigation sidebar -->
  <jsp:include page="sidebar.jsp" />

  <div class="dashboard-content">
    <header class="dashboard-header">
      <h1>Deposit Funds</h1>
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

        <form action="../deposit" method="post" class="form">
          <div class="form-group">
            <label for="amount">Amount to Deposit ($)</label>
            <input type="number" id="amount" name="amount" step="0.01" min="0.01" required>
          </div>

          <div class="form-group">
            <label for="description">Description (Optional)</label>
            <input type="text" id="description" name="description" placeholder="e.g., Salary, Gift">
          </div>

          <div class="form-group">
            <button type="submit" class="btn btn-primary">Deposit Funds</button>
            <a href="../dashboard/home.jsp" class="btn btn-outline">Cancel</a>
          </div>
        </form>
      </div>

      <!-- Deposit Tips -->
      <div class="card info-card">
        <h3>Deposit Information</h3>
        <ul class="info-list">
          <li>
            <strong>Instant Processing:</strong>
            All deposits are processed immediately and will reflect in your balance instantly.
          </li>
          <li>
            <strong>No Fees:</strong>
            SecureBank doesn't charge any fees for deposits to your account.
          </li>
          <li>
            <strong>Record Keeping:</strong>
            Adding a description helps you track your income sources.
          </li>
        </ul>
      </div>
    </div>
  </div>
</div>
</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
  <title>Bank Management System - Register</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      background-color: #f4f4f4;
      margin: 0;
      padding: 20px;
    }
    .register-container {
      max-width: 500px;
      margin: 50px auto;
      background-color: white;
      padding: 20px;
      border-radius: 5px;
      box-shadow: 0 0 10px rgba(0,0,0,0.1);
    }
    h2 {
      text-align: center;
      color: #333;
    }
    .form-group {
      margin-bottom: 15px;
    }
    label {
      display: block;
      margin-bottom: 5px;
      color: #666;
    }
    input[type="text"],
    input[type="email"],
    input[type="password"] {
      width: 100%;
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
      box-sizing: border-box;
    }
    .btn {
      background-color: #4CAF50;
      color: white;
      padding: 10px 15px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      width: 100%;
    }
    .btn:hover {
      background-color: #45a049;
    }
    .error-message {
      color: red;
      margin-bottom: 10px;
      text-align: center;
    }
    .login-link {
      text-align: center;
      margin-top: 15px;
    }
    .login-link a {
      color: #4CAF50;
      text-decoration: none;
    }
    .login-link a:hover {
      text-decoration: underline;
    }
  </style>
</head>
<body>
<div class="register-container">
  <h2>Register New Account</h2>

  <% if (request.getAttribute("error") != null) { %>
  <div class="error-message">
    <%= request.getAttribute("error") %>
  </div>
  <% } %>

  <form action="${pageContext.request.contextPath}/dashboard" method="post">
    <input type="hidden" name="action" value="createCustomer">

    <div class="form-group">
      <label for="firstName">First Name:</label>
      <input type="text" id="firstName" name="firstName" required>
    </div>

    <div class="form-group">
      <label for="lastName">Last Name:</label>
      <input type="text" id="lastName" name="lastName" required>
    </div>

    <div class="form-group">
      <label for="email">Email:</label>
      <input type="email" id="email" name="email" required>
    </div>

    <div class="form-group">
      <label for="phoneNumber">Phone Number:</label>
      <input type="text" id="phoneNumber" name="phoneNumber" required>
    </div>

    <div class="form-group">
      <label for="password">Password:</label>
      <input type="password" id="password" name="password" required>
    </div>

    <button type="submit" class="btn">Register</button>
  </form>

  <div class="login-link">
    <p>Already have an account? <a href="${pageContext.request.contextPath}/login.jsp">Login here</a></p>
  </div>
</div>
</body>
</html>

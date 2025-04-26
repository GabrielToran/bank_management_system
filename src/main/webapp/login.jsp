<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - SecureBank</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="container">
    <header>
        <h1>SecureBank</h1>
        <nav>
            <ul>
                <li><a href="index.jsp">Home</a></li>
                <li><a href="login.jsp" class="active">Login</a></li>
                <li><a href="#">About Us</a></li>
                <li><a href="#">Contact</a></li>
            </ul>
        </nav>
    </header>

    <main>
        <section class="login-container">
            <div class="login-card">
                <h2>Login to Your Account</h2>

                <%
                    String error = request.getParameter("error");
                    if (error != null && error.equals("true")) {
                %>
                <div class="error-message">
                    Invalid username or password. Please try again.
                </div>
                <% } %>

                <form action="LoginServlet" method="post" class="login-form">
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input type="text" id="username" name="username" required>
                    </div>

                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" required>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn primary">Login</button>
                        <div class="form-links">
                            <a href="#">Forgot Password?</a>
                        </div>
                    </div>
                </form>
            </div>
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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Withdraw Funds</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
<div class="container">
    <h1>Withdraw Funds</h1>

    <form action="banking" method="post">
        <input type="hidden" name="action" value="withdraw">

        <div class="form-group">
            <label>Account Number:</label>
            <input type="text" name="accountNumber" required>
        </div>

        <div class="form-group">
            <label>Amount:</label>
            <input type="number" name="amount" step="0.01" min="0.01" required>
        </div>

        <div class="form-group">
            <label>Description:</label>
            <input type="text" name="description">
        </div>

        <button type="submit" class="btn">Withdraw</button>
    </form>

    <div class="actions">
        <a href="banking?action=dashboard" class="btn">Back to Dashboard</a>
    </div>
</div>
</body>
</html>
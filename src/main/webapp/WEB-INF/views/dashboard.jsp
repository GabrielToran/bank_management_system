<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Banking Dashboard</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
<div class="container">
    <h1>Welcome to Your Banking Dashboard</h1>

    <div class="accounts-summary">
        <h2>Your Accounts</h2>
        <forEach var="account" items="${accounts}">
            <div class="account-card">
                <h3>${account.type} Account</h3>
                <p>Account Number: ${account.accountNumber}</p>
                <p>Balance: $${account.balance}</p>
                <a href="banking?action=transactions&accountNumber=${account.accountNumber}">View Transactions</a>
            </div>
        </forEach>
    </div>

    <div class="actions">
        <a href="banking?action=createAccount" class="btn">Open New Account</a>
        <a href="deposit.jsp" class="btn">Make Deposit</a>
        <a href="withdraw.jsp" class="btn">Make Withdrawal</a>
        <a href="transfer.jsp" class="btn">Transfer Funds</a>
    </div>
</div>
</body>
</html>
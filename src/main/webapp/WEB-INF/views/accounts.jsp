<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <title>My Accounts</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
<div class="container">
    <h1>My Accounts</h1>

    <table class="accounts-table">
        <thead>
        <tr>
            <th>Account Number</th>
            <th>Type</th>
            <th>Balance</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <forEach var="account" items="${accounts}">
            <tr>
                <td>${account.accountNumber}</td>
                <td>${account.type}</td>
                <td>$${account.balance}</td>
                <td>
                    <a href="banking?action=transactions&accountNumber=${account.accountNumber}">View Transactions</a>
                    <a href="deposit.jsp?accountNumber=${account.accountNumber}">Deposit</a>
                    <a href="withdraw.jsp?accountNumber=${account.accountNumber}">Withdraw</a>
                </td>
            </tr>
        </forEach>
        </tbody>
    </table>

    <div class="actions">
        <a href="banking?action=createAccount" class="btn">Open New Account</a>
        <a href="banking?action=dashboard" class="btn">Back to Dashboard</a>
    </div>
</div>
</body>
</html>

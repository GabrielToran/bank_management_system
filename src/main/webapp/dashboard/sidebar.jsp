<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String currentPage = request.getRequestURI();
%>

<div class="dashboard-sidebar">
    <div class="sidebar-header">
        <div class="logo">
            <h2>SecureBank</h2>
        </div>
    </div>

    <nav class="sidebar-menu">
        <ul>
            <li class="<%= currentPage.endsWith("/home.jsp") ? "active" : "" %>">
                <a href="<%= request.getContextPath() %>/dashboard/home.jsp">
                    <span class="menu-icon">🏠</span> Dashboard
                </a>
            </li>
            <li class="<%= currentPage.endsWith("/deposit.jsp") ? "active" : "" %>">
                <a href="<%= request.getContextPath() %>/deposit">
                    <span class="menu-icon">💰</span> Deposit
                </a>
            </li>
            <li class="<%= currentPage.endsWith("/withdraw.jsp") ? "active" : "" %>">
                <a href="<%= request.getContextPath() %>/withdraw">
                    <span class="menu-icon">💸</span> Withdraw
                </a>
            </li>
            <li class="<%= currentPage.endsWith("/transfer.jsp") ? "active" : "" %>">
                <a href="<%= request.getContextPath() %>/transfer">
                    <span class="menu-icon">🔄</span> Transfer
                </a>
            </li>
            <li class="<%= currentPage.endsWith("/history.jsp") ? "active" : "" %>">
                <a href="<%= request.getContextPath() %>/history">
                    <span class="menu-icon">📝</span> History
                </a>
            </li>
            <li class="<%= currentPage.endsWith("/profile.jsp") ? "active" : "" %>">
                <a href="<%= request.getContextPath() %>/profile">
                    <span class="menu-icon">👤</span> Profile
                </a>
            </li>
        </ul>
    </nav>

    <div class="sidebar-footer">
        <a href="<%= request.getContextPath() %>/logout" class="logout-btn">
            <span class="menu-icon">🚪</span> Logout
        </a>
    </div>
</div>
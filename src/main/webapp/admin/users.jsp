<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>SmartCart — Manage Users</title>
    <link rel="stylesheet" href="../css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body class="full-page">

<%
    String userName = (String) session.getAttribute("userName");

    List<Map<String, Object>> users =
            (List<Map<String, Object>>) request.getAttribute("users");
    if (users == null) users = new ArrayList<>();
%>

<!-- ===== ADMIN NAVBAR ===== -->
<nav class="navbar">
    <a href="dashboard.jsp" class="logo">SmartCart Admin 🛠️</a>

    <div class="nav-right">
        <a href="AdminServlet?action=products" class="cart-link">📦 Products</a>
        <a href="AdminServlet?action=users" class="cart-link cart-active">👥 Users</a>
        <a href="../ProductServlet" class="cart-link">🛍️ View Store</a>
        <span class="user-name">👤 <%= userName %></span>
        <a href="../LogoutServlet" class="logout-link">Logout</a>
    </div>
</nav>

<div class="admin-container">

    <h2 class="admin-title">All Users</h2>
    <p class="admin-subtitle"><%= users.size() %> registered users</p>

    <div class="admin-table-card admin-table-full">
        <table class="admin-table">
            <thead>
            <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Joined</th>
            </tr>
            </thead>
            <tbody>
            <% for (Map<String, Object> u : users) { %>
            <tr>
                <td><%= u.get("name") %></td>
                <td><%= u.get("email") %></td>
                <td>
                    <% if ("admin".equals(u.get("role"))) { %>
                    <span class="role-badge admin">Admin</span>
                    <% } else { %>
                    <span class="role-badge">Member</span>
                    <% } %>
                </td>
                <td><%= u.get("createdAt") %></td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>

</div>

</body>
</html>
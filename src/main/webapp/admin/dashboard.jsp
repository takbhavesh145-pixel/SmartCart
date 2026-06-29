<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>SmartCart — Admin Dashboard</title>
    <link rel="stylesheet" href="../css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body class="full-page">

<%
    // AdminFilter already blocks non-admins from reaching this page,
    // but we still grab the username to show a friendly greeting
    String userName = (String) session.getAttribute("userName");

    Map<String, Object> stats = (Map<String, Object>) request.getAttribute("stats");
    if (stats == null) stats = new HashMap<>();
%>

<!-- ===== ADMIN NAVBAR ===== -->
<nav class="navbar">
    <a href="dashboard.jsp" class="logo">SmartCart Admin 🛠️</a>

    <div class="nav-right">
        <a href="AdminServlet?action=products" class="cart-link">📦 Products</a>
        <a href="AdminServlet?action=users" class="cart-link">👥 Users</a>
        <a href="../ProductServlet" class="cart-link">🛍️ View Store</a>
        <span class="user-name">👤 <%= userName %></span>
        <a href="../LogoutServlet" class="logout-link">Logout</a>
    </div>
</nav>

<div class="admin-container">

    <h2 class="admin-title">Dashboard</h2>
    <p class="admin-subtitle">Overview of your store</p>

    <!-- ===== STATS GRID ===== -->
    <div class="admin-stats-grid">

        <div class="admin-stat-card">
            <div class="admin-stat-icon">👥</div>
            <p class="admin-stat-number"><%= stats.get("totalUsers") %></p>
            <p class="admin-stat-label">Total Users</p>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon">📦</div>
            <p class="admin-stat-number"><%= stats.get("totalProducts") %></p>
            <p class="admin-stat-label">Total Products</p>
        </div>

        <div class="admin-stat-card warning">
            <div class="admin-stat-icon">⚠️</div>
            <p class="admin-stat-number"><%= stats.get("outOfStock") %></p>
            <p class="admin-stat-label">Out of Stock</p>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon">🛒</div>
            <p class="admin-stat-number"><%= stats.get("totalCartItems") %></p>
            <p class="admin-stat-label">Items in Carts</p>
        </div>

    </div>

    <!-- ===== QUICK LINKS ===== -->
    <div class="admin-quick-links">
        <a href="AdminServlet?action=products" class="admin-link-card">
            <span class="admin-link-icon">📦</span>
            <div>
                <h3>Manage Products</h3>
                <p>Add, edit, or remove products from your store</p>
            </div>
        </a>

        <a href="AdminServlet?action=users" class="admin-link-card">
            <span class="admin-link-icon">👥</span>
            <div>
                <h3>View Users</h3>
                <p>See everyone registered on your platform</p>
            </div>
        </a>
    </div>

</div>

</body>
</html>
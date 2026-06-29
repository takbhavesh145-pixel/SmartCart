<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>SmartCart — My Account</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body class="full-page">

<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String userName = (String) session.getAttribute("userName");

    // Data sent by AccountServlet
    String accountName     = (String) request.getAttribute("accountName");
    String accountEmail    = (String) request.getAttribute("accountEmail");
    String accountRole     = (String) request.getAttribute("accountRole");
    Object memberSince     = request.getAttribute("memberSince");

    Object accountAgeObj       = request.getAttribute("accountAge");
    String accountInterests    = (String) request.getAttribute("accountInterests");

    Integer cartItemCount  = (Integer) request.getAttribute("cartItemCount");
    Integer cartTotalQty   = (Integer) request.getAttribute("cartTotalQty");

    // Get initials for the avatar circle, e.g. "Bhavesh Patel" -> "BP"
    String initials = "";
    if (accountName != null && !accountName.trim().isEmpty()) {
        String[] parts = accountName.trim().split("\\s+");
        initials = parts[0].substring(0, 1).toUpperCase();
        if (parts.length > 1) {
            initials += parts[parts.length - 1].substring(0, 1).toUpperCase();
        }
    }
%>

<!-- ===== NAVBAR (same as other pages) ===== -->
<nav class="navbar">
    <a href="ProductServlet" class="logo">SmartCart 🛍️</a>

    <form action="ProductServlet" method="get" class="search-form">
        <input type="text" name="search" placeholder="Search products..." class="search-input">
        <button type="submit" class="search-btn">🔍</button>
    </form>

    <div class="nav-right">
        <a href="CartServlet" class="cart-link">🛒 Cart</a>
        <a href="AccountServlet" class="cart-link cart-active">👤 <%= userName %></a>
        <a href="LogoutServlet" class="logout-link">Logout</a>
    </div>
</nav>

<div class="account-container">

    <h2 class="account-title">My Account</h2>

    <div class="account-layout">

        <!-- ===== LEFT: Profile card ===== -->
        <div class="account-card">
            <div class="account-avatar"><%= initials %></div>
            <h3 class="account-name"><%= accountName %></h3>
            <p class="account-email"><%= accountEmail %></p>

            <% if ("admin".equals(accountRole)) { %>
            <span class="role-badge admin">Admin</span>
            <% } else { %>
            <span class="role-badge">Member</span>
            <% } %>

            <div class="account-meta">
                <p>Member since: <%= memberSince %></p>
            </div>
        </div>

        <!-- ===== RIGHT: Details + stats ===== -->
        <div class="account-details">

            <!-- Quick stats -->
            <div class="stats-grid">
                <div class="stat-box">
                    <p class="stat-number"><%= cartTotalQty != null ? cartTotalQty : 0 %></p>
                    <p class="stat-label">Items in Cart</p>
                </div>
                <div class="stat-box">
                    <p class="stat-number"><%= cartItemCount != null ? cartItemCount : 0 %></p>
                    <p class="stat-label">Unique Products</p>
                </div>
            </div>

            <!-- Preferences section -->
            <div class="detail-section">
                <h3>Shopping Preferences</h3>

                <% if (accountAgeObj != null) { %>
                <div class="detail-row">
                    <span class="detail-label">Age</span>
                    <span class="detail-value"><%= accountAgeObj %></span>
                </div>

                <div class="detail-row">
                    <span class="detail-label">Interests</span>
                    <span class="detail-value">
                            <% if (accountInterests != null) {
                                for (String interest : accountInterests.split(",")) { %>
                                    <span class="interest-tag"><%= interest %></span>
                            <% } } %>
                        </span>
                </div>
                <% } else { %>
                <p class="no-prefs">You haven't set your preferences yet.</p>
                <a href="preference.jsp" class="btn-primary btn-small">Set Preferences</a>
                <% } %>
            </div>

            <!-- Quick actions -->
            <div class="detail-section">
                <h3>Quick Actions</h3>
                <div class="action-links">
                    <a href="CartServlet" class="action-link">🛒 View Cart</a>
                    <a href="ProductServlet" class="action-link">🛍️ Continue Shopping</a>
                    <a href="LogoutServlet" class="action-link danger">🚪 Logout</a>
                </div>
            </div>

        </div>
    </div>

</div>

</body>
</html>
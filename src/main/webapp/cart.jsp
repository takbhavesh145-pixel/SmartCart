<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>SmartCart — My Cart</title>
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

    List<Map<String, Object>> cartItems =
            (List<Map<String, Object>>) request.getAttribute("cartItems");
    Double  grandTotal = (Double)  request.getAttribute("grandTotal");
    Integer totalItems = (Integer) request.getAttribute("totalItems");

    if (cartItems  == null) cartItems  = new ArrayList<>();
    if (grandTotal == null) grandTotal = 0.0;
    if (totalItems == null) totalItems = 0;
%>

<!-- ===== NAVBAR ===== -->
<nav class="navbar">
    <a href="ProductServlet" class="logo">SmartCart 🛍️</a>

    <form action="ProductServlet" method="get" class="search-form">
        <input type="text" name="search" placeholder="Search products..." class="search-input">
        <button type="submit" class="search-btn">🔍</button>
    </form>

    <div class="nav-right">
        <a href="CartServlet" class="cart-link cart-active">🛒 Cart (<%= totalItems %>)</a>
        <span class="user-name">👤 <%= userName %></span>
        <a href="LogoutServlet" class="logout-link">Logout</a>
    </div>
</nav>

<div class="cart-container">

    <h2 class="cart-title">🛒 My Cart <span class="cart-count-badge"><%= totalItems %> items</span></h2>

    <% if (cartItems.isEmpty()) { %>

    <div class="empty-cart">
        <div style="font-size: 80px;">🛒</div>
        <h3>Your cart is empty!</h3>
        <p>Looks like you haven't added anything yet</p>
        <a href="ProductServlet" class="btn-primary">Start Shopping</a>
    </div>

    <% } else { %>

    <div class="cart-layout">

        <div class="cart-items">

            <form action="CartServlet" method="post" class="clear-form">
                <input type="hidden" name="action" value="clear">
                <button type="submit" class="btn-clear"
                        onclick="return confirm('Clear entire cart?')">🗑️ Clear Cart</button>
            </form>

            <% for (Map<String, Object> item : cartItems) { %>

            <div class="cart-item">
                <img src="<%= item.get("image") %>" alt="<%= item.get("name") %>"
                     class="cart-item-image"
                     onerror="this.src='https://placehold.co/100x100?text=No+Image'">

                <div class="cart-item-details">
                    <h3 class="cart-item-name"><%= item.get("name") %></h3>
                    <p class="cart-item-price">
                        ₹<%= String.format("%,.0f", (double) item.get("price")) %> per item
                    </p>

                    <div class="quantity-controls">
                        <form action="CartServlet" method="post">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="cartId" value="<%= item.get("cartId") %>">
                            <input type="hidden" name="quantity" value="<%= (int)item.get("quantity") - 1 %>">
                            <button type="submit" class="qty-btn">−</button>
                        </form>

                        <span class="qty-display"><%= item.get("quantity") %></span>

                        <form action="CartServlet" method="post">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="cartId" value="<%= item.get("cartId") %>">
                            <input type="hidden" name="quantity" value="<%= (int)item.get("quantity") + 1 %>">
                            <button type="submit" class="qty-btn"
                                    <%= (int)item.get("quantity") >= (int)item.get("stock") ? "disabled" : "" %>>+</button>
                        </form>
                    </div>
                </div>

                <div class="cart-item-right">
                    <p class="cart-item-total">
                        ₹<%= String.format("%,.0f", (double) item.get("itemTotal")) %>
                    </p>
                    <form action="CartServlet" method="post">
                        <input type="hidden" name="action" value="remove">
                        <input type="hidden" name="cartId" value="<%= item.get("cartId") %>">
                        <button type="submit" class="btn-remove">🗑️ Remove</button>
                    </form>
                </div>
            </div>

            <% } %>

        </div>

        <div class="cart-summary">
            <h3 class="summary-title">Order Summary</h3>

            <div class="summary-row">
                <span>Items (<%= totalItems %>)</span>
                <span>₹<%= String.format("%,.0f", grandTotal) %></span>
            </div>

            <div class="summary-row">
                <span>Delivery</span>
                <% if (grandTotal > 500) { %>
                <span class="free-delivery">FREE</span>
                <% } else { %>
                <span>₹50</span>
                <% } %>
            </div>

            <div class="summary-divider"></div>

            <div class="summary-row total-row">
                <span>Grand Total</span>
                <span>
                        <% double delivery = grandTotal > 500 ? 0 : 50;
                            double finalTotal = grandTotal + delivery; %>
                        ₹<%= String.format("%,.0f", finalTotal) %>
                    </span>
            </div>

            <% if (grandTotal <= 500) { %>
            <p class="free-delivery-msg">
                Add ₹<%= String.format("%,.0f", 500 - grandTotal) %> more for FREE delivery!
            </p>
            <% } else { %>
            <p class="free-delivery-msg green">You got FREE delivery! 🎉</p>
            <% } %>

            <a href="checkout.jsp" class="btn-checkout">Proceed to Checkout →</a>
            <a href="ProductServlet" class="btn-continue">← Continue Shopping</a>
        </div>
    </div>

    <% } %>

</div>

</body>
</html>
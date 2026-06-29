<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>SmartCart — Shop Everything</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body class="full-page">


<%
//    Security check — redirect to login if not authenticated
    if (session.getAttribute("userId") == null) {
       response.sendRedirect("login.jsp");
       return;
   }

    String userName = (String) session.getAttribute("userName");

    // Data sent by ProductServlet
    List<Map<String, Object>> products =
            (List<Map<String, Object>>) request.getAttribute("products");
    String currentCategory = (String) request.getAttribute("category");
    String currentSearch   = (String) request.getAttribute("search");

    if (currentCategory == null) currentCategory = "";
    if (currentSearch   == null) currentSearch   = "";
%>

<!-- ===== NAVBAR ===== -->
<nav class="navbar">
    <a href="ProductServlet" class="logo">SmartCart 🛍️</a>

    <form action="ProductServlet" method="get" class="search-form">
        <input type="text" name="search" placeholder="Search products..."
               value="<%= currentSearch %>" class="search-input">
        <button type="submit" class="search-btn">🔍</button>
    </form>

    <div class="nav-right">
        <a href="CartServlet" class="cart-link">🛒 Cart</a>
        <a href="AccountServlet" class="cart-link">👤 <%= userName %></a>
        <%-- IMPORTANT: points to LogoutServlet now, not login.jsp directly —
             this actually destroys the session instead of just showing a page --%>
        <a href="LogoutServlet" class="logout-link">Logout</a>
    </div>
</nav>

<!-- ===== CATEGORY FILTER BAR ===== -->
<section class="categories-section">
    <div class="categories">
        <a href="ProductServlet"
           class="category-btn <%= currentCategory.isEmpty() ? "active" : "" %>">All</a>

        <a href="ProductServlet?category=Electronics"
           class="category-btn <%= currentCategory.equals("Electronics") ? "active" : "" %>">📱 Electronics</a>

        <a href="ProductServlet?category=Fashion"
           class="category-btn <%= currentCategory.equals("Fashion") ? "active" : "" %>">👗 Fashion</a>

        <a href="ProductServlet?category=Sports"
           class="category-btn <%= currentCategory.equals("Sports") ? "active" : "" %>">⚽ Sports</a>

        <a href="ProductServlet?category=Books"
           class="category-btn <%= currentCategory.equals("Books") ? "active" : "" %>">📚 Books</a>

        <a href="ProductServlet?category=Beauty"
           class="category-btn <%= currentCategory.equals("Beauty") ? "active" : "" %>">💄 Beauty</a>

        <a href="ProductServlet?category=Home & Kitchen"
           class="category-btn <%= currentCategory.equals("Home & Kitchen") ? "active" : "" %>">🏠 Home & Kitchen</a>
    </div>
</section>

<!-- ===== PAGE TITLE ===== -->
<section class="page-title">
    <% if (!currentSearch.isEmpty()) { %>
    <h2>Search results for "<%= currentSearch %>"</h2>
    <% } else if (!currentCategory.isEmpty()) { %>
    <h2><%= currentCategory %></h2>
    <% } else { %>
    <h2>Hi <%= userName %> 👋 Recommended for you</h2>
    <% } %>
    <p class="product-count"><%= products != null ? products.size() : 0 %> products found</p>
</section>

<!-- ===== PRODUCTS GRID ===== -->
<section class="products-wrapper">
    <div class="products-grid">

        <% if (products == null || products.isEmpty()) { %>

        <div class="no-products">
            <div class="empty-icon">😕</div>
            <h3>No products found</h3>
            <a href="ProductServlet" class="btn-primary">View All Products</a>
        </div>

        <% } else {
            for (Map<String, Object> product : products) { %>

        <div class="product-card">
            <div class="product-image-box">
                <img src="<%= product.get("image") %>"
                     alt="<%= product.get("name") %>"
                     class="product-image"
                     onerror="this.src='https://placehold.co/300x300?text=No+Image'">
            </div>

            <div class="product-info">
                <span class="category-badge"><%= product.get("category") %></span>
                <h3 class="product-name"><%= product.get("name") %></h3>

                <div class="rating">
                    <%
                        double rating = (double) product.get("rating");
                        int stars = (int) Math.round(rating);
                        for (int i = 0; i < stars; i++) {
                    %>
                    <span class="star filled">★</span>
                    <% } %>
                    <% for (int i = stars; i < 5; i++) { %>
                    <span class="star">★</span>
                    // Result: ★★★★☆
                    <% } %>
                    <span class="rating-number">(<%= rating %>)</span>
                </div>
                <%-- 1500000.99 ----> "1,500,001"--%>
                <div class="price">₹<%= String.format("%,.0f", (double) product.get("price")) %></div>

                <% int stock = (int) product.get("stock"); %>
                <% if (stock > 0) { %>
                <span class="in-stock">✅ In Stock (<%= stock %> left)</span>
                <% } else { %>
                <span class="out-of-stock">❌ Out of Stock</span>
                <% } %>

                 <%-- when stock is zero then butten disabled              --%>
                <form action="CartServlet" method="post" class="cart-form">
                    <input type="hidden" name="productId" value="<%= product.get("id") %>">
                    <input type="hidden" name="action" value="add">
                    <button type="submit" class="add-to-cart-btn"
                            <%= stock == 0 ? "disabled" : "" %>>🛒 Add to Cart</button>
                </form>
            </div>
        </div>

        <% } } %>

    </div>
</section>

<!-- ============================================================
     AI CHATBOT WIDGET
     A floating chat bubble in the bottom-right corner.
     Talks to ChatbotServlet using fetch() (JavaScript AJAX) —
     this means the page does NOT reload when you send a message,
     which is the standard pattern for any modern chat interface.
     ============================================================ -->

<!-- The small round button that opens/closes the chat window -->
<button id="chatToggleBtn" class="chat-toggle-btn" onclick="toggleChat()">
    💬
</button>

<!-- The chat window itself — hidden by default via CSS -->
<div id="chatWindow" class="chat-window">

    <div class="chat-header">
        <span>🤖 SmartCart Assistant</span>
        <button onclick="toggleChat()" class="chat-close-btn">✕</button>
    </div>

    <!-- Messages get added here dynamically by hardcoded html -->
    <div id="chatMessages" class="chat-messages">
        <div class="chat-message bot">
            Hi! I can help you find products. Try asking "Show me Electronics" or "Books under 500"
        </div>
    </div>

    <form id="chatForm" class="chat-input-form" onsubmit="sendChatMessage(event)">
        <input type="text" id="chatInput" placeholder="Ask me anything..." autocomplete="off">
        <button type="submit">➤</button>
    </form>

</div>

<script>
    // ===== Toggle chat window open/closed =====
    function toggleChat() {
        const chatWindow = document.getElementById('chatWindow');
        // — If "chat-open" class is NOT present → ADDS it   → window appears
        // — If "chat-open" class IS present     → REMOVES it → window hides
        chatWindow.classList.toggle('chat-open');
    }

    // ===== Send a message to the chatbot =====
             // why asyanc is added
    // Browser WAITS here... 2 seconds...
    // During this wait — PAGE IS FROZEN
    // User can't click anything, scroll, type
    // Like a hanging app
    async function sendChatMessage(event) {
        // Prevents default form behavior (page reload on submit)
        // Without this line → page refreshes and chat history is lost
        event.preventDefault();

        const input = document.getElementById('chatInput');
        const message = input.value.trim();

        if (message === '') return; // ignore empty messages

        // Show the user's own message in the chat window immediately
        addMessageToChat(message, 'user');

        // Clear the input box for the next message
        input.value = '';

        // Show a temporary "typing..." indicator while we wait for the server
        const typingId = addMessageToChat('Thinking...', 'bot');

        try {
            // fetch() sends an HTTP request WITHOUT reloading the page.
            // INTERVIEW CONCEPT: this is "AJAX" (Asynchronous JavaScript And XML,
            // though these days it's almost always JSON, not XML).
            // It's how every modern web app (Gmail, Instagram, etc.) updates
            // parts of a page without a full reload.
            const response = await fetch('ChatbotServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'message=' + encodeURIComponent(message)
            });

            const data = await response.json();

            // Replace the "Thinking..." message with the real reply
            document.getElementById(typingId).innerText = data.reply;

        } catch (error) {
            document.getElementById(typingId).innerText =
                'Sorry, something went wrong. Please try again.';
            console.error('Chatbot error:', error);
        }
    }

    // ===== Helper: adds a message bubble to the chat window =====
    // Returns the new message's id so we can update it later (for "Thinking...")
    function addMessageToChat(text, sender) {
        const messagesDiv = document.getElementById('chatMessages');

        const messageEl = document.createElement('div');
        const uniqueId = 'msg-' + Date.now() + '-' + Math.random().toString(36).slice(2);

        messageEl.id = uniqueId;
        messageEl.className = 'chat-message ' + sender; // "chat-message user" or "chat-message bot"
        messageEl.innerText = text; // innerText (not innerHTML) — prevents XSS injection
        // from anything a user might type into the chat

        messagesDiv.appendChild(messageEl);

        // Auto-scroll to the newest message
        messagesDiv.scrollTop = messagesDiv.scrollHeight;

        return uniqueId;
    }
</script>

</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>SmartCart — Login</title>
    <!-- Links to our external stylesheet -->
    <link rel="stylesheet" href="css/style.css">
    <!-- Google Font — gives a modern, clean look instead of default browser font -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body class="auth-page">

<div class="auth-container">

    <!-- Left side: branding panel (only visible on larger screens) -->
    <div class="auth-brand">
        <div class="auth-brand-content">
            <h1 class="auth-logo">SmartCart 🛍️</h1>
            <p class="auth-tagline">Shop smarter, not harder</p>
            <ul class="auth-features">
                <li>✓ Personalized recommendations</li>
                <li>✓ Secure checkout</li>
                <li>✓ Fast delivery</li>
            </ul>
        </div>
    </div>

    <!-- Right side: the actual login form -->
    <div class="auth-form-side">
        <div class="auth-box">

            <h2>Welcome back</h2>
            <p class="auth-subtitle">Login to continue shopping</p>

            <%-- Show error message if login failed --%>
            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null) { %>
            <div class="alert alert-error"><%= error %></div>
            <% } %>

            <%-- Show success message after registering --%>
            <% if ("true".equals(request.getParameter("registered"))) { %>
            <div class="alert alert-success">Account created! Please log in.</div>
            <% } %>

            <form action="LoginServlet" method="post" class="auth-form">

                <div class="form-field">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email"
                           placeholder="you@example.com" required>
                </div>

                <div class="form-field">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password"
                           placeholder="Enter your password" required>
                </div>

                <button type="submit" class="btn-primary">Login</button>
            </form>

            <p class="auth-switch">
                Don't have an account? <a href="register.jsp">Create one</a>
            </p>

        </div>
    </div>

</div>

</body>
</html>
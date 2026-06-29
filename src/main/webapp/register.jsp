<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>SmartCart — Register</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body class="auth-page">

<div class="auth-container">

    <!-- Left branding panel — same as login.jsp for consistency -->
    <div class="auth-brand">
        <div class="auth-brand-content">
            <h1 class="auth-logo">SmartCart 🛍️</h1>
            <p class="auth-tagline">Join thousands of happy shoppers</p>
            <ul class="auth-features">
                <li>✓ Personalized recommendations</li>
                <li>✓ Secure checkout</li>
                <li>✓ Fast delivery</li>
            </ul>
        </div>
    </div>

    <!-- Right side: registration form -->
    <div class="auth-form-side">
        <div class="auth-box">

            <h2>Create your account</h2>
            <p class="auth-subtitle">Start shopping in seconds</p>

            <%-- Show error if registration failed (e.g. email already exists) --%>
            <%-- IMPORTANT: this "error" variable is declared ONLY ONCE in this file.
                 Declaring it twice causes a "Duplicate local variable error"
                 compile error in Tomcat — this was the bug from your screenshot. --%>
            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null) { %>
            <div class="alert alert-error"><%= error %></div>
            <% } %>

            <form action="RegisterServlet" method="post" class="auth-form">

                <div class="form-field">
                    <label for="name">Full Name</label>
                    <input type="text" id="name" name="name"
                           placeholder="John Doe" required>
                </div>

                <div class="form-field">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email"
                           placeholder="you@example.com" required>
                </div>

                <div class="form-field">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password"
                           placeholder="At least 8 characters" required minlength="8">
                    <%-- minlength gives instant browser feedback, but remember:
                         the REAL check happens server-side in RegisterServlet.
                         This is just a nicer UX hint, never the actual security boundary --%>
                </div>

                <button type="submit" class="btn-primary btn-full">Create Account</button>
            </form>

            <p class="auth-switch">
                Already have an account? <a href="login.jsp">Login</a>
            </p>

        </div>
    </div>

</div>

</body>
</html>
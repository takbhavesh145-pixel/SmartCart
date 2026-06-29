<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>SmartCart — Tell Us About You</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body class="center-page">

<div class="preference-box">

    <div class="preference-header">
        <h1>Welcome to SmartCart! 🛍️</h1>
        <p>Help us personalize your shopping experience</p>
    </div>

    <%-- Show error if validation failed in PreferenceServlet --%>
    <% String error = (String) request.getAttribute("error"); %>
    <% if (error != null) { %>
    <div class="alert alert-error"><%= error %></div>
    <% } %>

    <form action="PreferenceServlet" method="post">

        <!-- Age input -->
        <div class="form-group">
            <label>How old are you?</label>
            <input type="number" name="age" placeholder="Enter your age"
                   min="10" max="120" required>
        </div>

        <!-- Interest checkboxes -->
        <div class="form-group">
            <label>What are you interested in?</label>

            <div class="interests-grid">
                <label class="interest-item">
                    <input type="checkbox" name="interests" value="Electronics">
                    <span>📱 Electronics</span>
                </label>

                <label class="interest-item">
                    <input type="checkbox" name="interests" value="Fashion">
                    <span>👗 Fashion</span>
                </label>

                <label class="interest-item">
                    <input type="checkbox" name="interests" value="Sports">
                    <span>⚽ Sports</span>
                </label>

                <label class="interest-item">
                    <input type="checkbox" name="interests" value="Books">
                    <span>📚 Books</span>
                </label>

                <label class="interest-item">
                    <input type="checkbox" name="interests" value="Beauty">
                    <span>💄 Beauty</span>
                </label>

                <label class="interest-item">
                    <input type="checkbox" name="interests" value="Home & Kitchen">
                    <span>🏠 Home & Kitchen</span>
                </label>
            </div>
        </div>

        <button type="submit" class="btn-primary btn-full">Start Shopping 🚀</button>

    </form>
</div>

</body>
</html>
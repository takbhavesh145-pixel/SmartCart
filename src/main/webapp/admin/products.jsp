<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>SmartCart — Manage Products</title>
    <link rel="stylesheet" href="../css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body class="full-page">

<%
    String userName = (String) session.getAttribute("userName");

    List<Map<String, Object>> products =
            (List<Map<String, Object>>) request.getAttribute("products");
    if (products == null) products = new ArrayList<>();

    // If we're editing a product, this will be filled — otherwise null
    // (null means the form below is in "Add New Product" mode)
    Map<String, Object> editProduct =
            (Map<String, Object>) request.getAttribute("editProduct");
%>

<!-- ===== ADMIN NAVBAR ===== -->
<nav class="navbar">
    <a href="dashboard.jsp" class="logo">SmartCart Admin 🛠️</a>

    <div class="nav-right">
        <a href="AdminServlet?action=products" class="cart-link cart-active">📦 Products</a>
        <a href="AdminServlet?action=users" class="cart-link">👥 Users</a>
        <a href="../ProductServlet" class="cart-link">🛍️ View Store</a>
        <span class="user-name">👤 <%= userName %></span>
        <a href="../LogoutServlet" class="logout-link">Logout</a>
    </div>
</nav>

<div class="admin-container">

    <h2 class="admin-title">Manage Products</h2>

    <%-- Show success/error messages based on URL parameters --%>
    <% if ("true".equals(request.getParameter("saved"))) { %>
    <div class="alert alert-success">Product saved successfully!</div>
    <% } %>
    <% if ("true".equals(request.getParameter("deleted"))) { %>
    <div class="alert alert-success">Product deleted.</div>
    <% } %>
    <% if (request.getParameter("error") != null) { %>
    <div class="alert alert-error">Something went wrong. Please check your input.</div>
    <% } %>

    <div class="admin-layout">

        <!-- ===== LEFT: Add/Edit form ===== -->
        <div class="admin-form-card">
            <h3><%= editProduct != null ? "Edit Product" : "Add New Product" %></h3>

            <form action="AdminServlet" method="post" class="admin-product-form">
                <input type="hidden" name="action" value="saveProduct">

                <%-- If editing, this hidden field tells the Servlet to UPDATE
                     instead of INSERT. Empty/missing means "new product". --%>
                <% if (editProduct != null) { %>
                <input type="hidden" name="id" value="<%= editProduct.get("id") %>">
                <% } %>

                <div class="form-field">
                    <label>Product Name</label>
                    <input type="text" name="name" required
                           value="<%= editProduct != null ? editProduct.get("name") : "" %>">
                </div>

                <div class="form-field">
                    <label>Description</label>
                    <textarea name="description" rows="3"><%= editProduct != null ? editProduct.get("description") : "" %></textarea>
                </div>

                <div class="form-field">
                    <label>Category</label>
                    <select name="category" required>
                        <%
                            String[] categories = {"Electronics", "Fashion", "Sports", "Books", "Beauty", "Home & Kitchen"};
                            String currentCategory = editProduct != null ? (String) editProduct.get("category") : "";
                            for (String cat : categories) {
                        %>
                        <option value="<%= cat %>" <%= cat.equals(currentCategory) ? "selected" : "" %>><%= cat %></option>
                        <% } %>
                    </select>
                </div>

                <div class="form-row">
                    <div class="form-field">
                        <label>Price (₹)</label>
                        <input type="number" name="price" step="0.01" min="0" required
                               value="<%= editProduct != null ? editProduct.get("price") : "" %>">
                    </div>

                    <div class="form-field">
                        <label>Stock</label>
                        <input type="number" name="stock" min="0" required
                               value="<%= editProduct != null ? editProduct.get("stock") : "" %>">
                    </div>
                </div>

                <div class="form-field">
                    <label>Image URL</label>
                    <input type="text" name="image"
                           value="<%= editProduct != null ? editProduct.get("image") : "" %>">
                </div>

                <button type="submit" class="btn-primary btn-full">
                    <%= editProduct != null ? "Update Product" : "Add Product" %>
                </button>

                <% if (editProduct != null) { %>
                <a href="AdminServlet?action=products" class="btn-continue" style="margin-top: 10px;">Cancel Edit</a>
                <% } %>
            </form>
        </div>

        <!-- ===== RIGHT: Product list table ===== -->
        <div class="admin-table-card">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Category</th>
                    <th>Price</th>
                    <th>Stock</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <% for (Map<String, Object> p : products) { %>
                <tr>
                    <td><%= p.get("name") %></td>
                    <td><span class="category-badge"><%= p.get("category") %></span></td>
                    <td>₹<%= String.format("%,.0f", (double) p.get("price")) %></td>
                    <td>
                        <% int stock = (int) p.get("stock"); %>
                        <% if (stock == 0) { %>
                        <span class="stock-zero"><%= stock %></span>
                        <% } else { %>
                        <%= stock %>
                        <% } %>
                    </td>
                    <td class="admin-table-actions">
                        <a href="AdminServlet?action=products&editId=<%= p.get("id") %>" class="btn-edit">Edit</a>
                        <a href="AdminServlet?action=deleteProduct&id=<%= p.get("id") %>"
                           class="btn-delete"
                           onclick="return confirm('Delete this product? This cannot be undone.')">Delete</a>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>

    </div>

</div>

</body>
</html>
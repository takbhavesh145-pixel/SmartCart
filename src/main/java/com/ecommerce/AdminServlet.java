// This file belongs to com.ecommerce package
package com.ecommerce;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AdminServlet — handles everything for the admin dashboard:
 * viewing stats, listing/adding/editing/deleting products, viewing users.
 *
 * INTERVIEW CONCEPT: this Servlet is protected by AdminFilter
 * (urlPatterns="/admin/*"), so by the time any code here runs,
 * we already know the user is a confirmed admin. No need to
 * re-check role here — that's the whole point of using a Filter.
 */
@WebServlet("/admin/AdminServlet")
public class AdminServlet extends HttpServlet {

    // ===== doGet handles VIEWING pages: dashboard, product list, user list =====
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // "action" tells us which admin view to load
        // e.g. admin/AdminServlet?action=products
        String action = request.getParameter("action");
        if (action == null) action = "dashboard";

        switch (action) {
            case "products":
                showProducts(request, response);
                break;
            case "users":
                showUsers(request, response);
                break;
            case "deleteProduct":
                deleteProduct(request, response);
                break;
            default:
                showDashboard(request, response);
        }
    }

    // ===== doPost handles SAVING: add product, edit product =====
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("saveProduct".equals(action)) {
            saveProduct(request, response);
        } else {
            response.sendRedirect("AdminServlet");
        }
    }

    // ===== DASHBOARD: shows summary stats =====
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try (Connection con = DBConnection.getConnection()) {

            Map<String, Object> stats = new HashMap<>();

            // Total registered users
            stats.put("totalUsers", countRows(con, "SELECT COUNT(*) FROM users"));

            // Total products in catalog
            stats.put("totalProducts", countRows(con, "SELECT COUNT(*) FROM products"));

            // Products that are out of stock — useful "needs attention" metric
            stats.put("outOfStock", countRows(con,
                    "SELECT COUNT(*) FROM products WHERE stock = 0"));

            // Total items currently sitting in everyone's carts combined
            stats.put("totalCartItems", countRows(con,
                    "SELECT COALESCE(SUM(quantity), 0) FROM cart"));

            request.setAttribute("stats", stats);
            request.getRequestDispatcher("admin/dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Small helper — runs a SELECT COUNT(*) style query and returns the number
    private int countRows(Connection con, String sql) throws Exception {
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        return rs.next() ? rs.getInt(1) : 0;
    }

    // ===== PRODUCT LIST: shows all products with edit/delete options =====
    private void showProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM products ORDER BY id DESC"
            );
            ResultSet rs = ps.executeQuery();

            List<Map<String, Object>> products = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id",       rs.getInt("id"));
                p.put("name",     rs.getString("name"));
                p.put("category", rs.getString("category"));
                p.put("price",    rs.getDouble("price"));
                p.put("stock",    rs.getInt("stock"));
                p.put("image",    rs.getString("image"));
                products.add(p);
            }

            request.setAttribute("products", products);

            // If we're editing a specific product, load its full details too
            String editId = request.getParameter("editId");
            if (editId != null) {
                PreparedStatement editPs = con.prepareStatement(
                        "SELECT * FROM products WHERE id = ?"
                );
                editPs.setInt(1, Integer.parseInt(editId));
                ResultSet editRs = editPs.executeQuery();
                if (editRs.next()) {
                    Map<String, Object> editProduct = new HashMap<>();
                    editProduct.put("id",          editRs.getInt("id"));
                    editProduct.put("name",        editRs.getString("name"));
                    editProduct.put("description", editRs.getString("description"));
                    editProduct.put("category",    editRs.getString("category"));
                    editProduct.put("price",       editRs.getDouble("price"));
                    editProduct.put("stock",       editRs.getInt("stock"));
                    editProduct.put("image",       editRs.getString("image"));
                    request.setAttribute("editProduct", editProduct);
                }
            }

            request.getRequestDispatcher("admin/products.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== SAVE PRODUCT: handles BOTH adding a new product AND editing one =====
    private void saveProduct(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String idStr       = request.getParameter("id"); // empty/null = new product
        String name        = request.getParameter("name");
        String description = request.getParameter("description");
        String category    = request.getParameter("category");
        String priceStr     = request.getParameter("price");
        String stockStr     = request.getParameter("stock");
        String image        = request.getParameter("image");

        // ===== Basic server-side validation =====
        if (name == null || name.trim().isEmpty()) {
            response.sendRedirect("AdminServlet?action=products&error=missingname");
            return;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("AdminServlet?action=products&error=invalidnumber");
            return;
        }

        if (price < 0 || stock < 0) {
            response.sendRedirect("AdminServlet?action=products&error=negativevalue");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            boolean isNewProduct = (idStr == null || idStr.trim().isEmpty());

            if (isNewProduct) {
                // ===== INSERT a brand new product =====
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO products (name, description, category, price, stock, image) " +
                                "VALUES (?, ?, ?, ?, ?, ?)"
                );
                ps.setString(1, name.trim());
                ps.setString(2, description);
                ps.setString(3, category);
                ps.setDouble(4, price);
                ps.setInt(5, stock);
                ps.setString(6, image);
                ps.executeUpdate();

            } else {
                // ===== UPDATE an existing product =====
                int id = Integer.parseInt(idStr);
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE products SET name=?, description=?, category=?, " +
                                "price=?, stock=?, image=? WHERE id=?"
                );
                ps.setString(1, name.trim());
                ps.setString(2, description);
                ps.setString(3, category);
                ps.setDouble(4, price);
                ps.setInt(5, stock);
                ps.setString(6, image);
                ps.setInt(7, id);
                ps.executeUpdate();
            }

            response.sendRedirect("AdminServlet?action=products&saved=true");

        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendRedirect("AdminServlet?action=products&error=savefailed");
            } catch (IOException ignored) {}
        }
    }

    // ===== DELETE PRODUCT =====
    private void deleteProduct(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String idStr = request.getParameter("id");

        try (Connection con = DBConnection.getConnection()) {
            int id = Integer.parseInt(idStr);

            // Thanks to ON DELETE CASCADE in our schema, deleting a product
            // automatically removes any matching rows from the cart table too
            PreparedStatement ps = con.prepareStatement("DELETE FROM products WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();

            response.sendRedirect("AdminServlet?action=products&deleted=true");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== USER LIST: shows all registered users =====
    private void showUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT id, name, email, role, created_at FROM users ORDER BY id DESC"
            );
            ResultSet rs = ps.executeQuery();

            List<Map<String, Object>> users = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> u = new HashMap<>();
                u.put("id",        rs.getInt("id"));
                u.put("name",      rs.getString("name"));
                u.put("email",     rs.getString("email"));
                u.put("role",      rs.getString("role"));
                u.put("createdAt", rs.getTimestamp("created_at"));
                users.add(u);
            }

            request.setAttribute("users", users);
            request.getRequestDispatcher("admin/users.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
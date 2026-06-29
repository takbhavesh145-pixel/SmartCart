// This file belongs to com.ecommerce package
package com.ecommerce;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// ======================= COLLECTIONS =======================

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ======================= SERVLET IMPORTS =======================

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ProductServlet")
public class ProductServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // Security check — must be logged in
        if (session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Must have set preferences first (your original flow)
        if (session.getAttribute("hasPreferences") == null) {
            response.sendRedirect("preference.jsp");
            return;
        }

        String category = request.getParameter("category");
        String search    = request.getParameter("search");

        try (Connection con = DBConnection.getConnection()) {

            String sql;

            // Decide which query to run based on what the user is doing
            if (search != null && !search.trim().isEmpty()) {
                // % wildcards mean "contains this text anywhere"
                sql = "SELECT * FROM products WHERE name LIKE ? ORDER BY rating DESC";
            } else if (category != null && !category.trim().isEmpty()) {
                sql = "SELECT * FROM products WHERE category = ? ORDER BY rating DESC";
            } else {
                sql = "SELECT * FROM products ORDER BY rating DESC";
            }

            PreparedStatement ps = con.prepareStatement(sql);

            // Fill in the ? based on which filter is active
            if (search != null && !search.trim().isEmpty()) {
                ps.setString(1, "%" + search.trim() + "%");
            } else if (category != null && !category.trim().isEmpty()) {
                ps.setString(1, category.trim());
            }

            ResultSet rs = ps.executeQuery();

            List<Map<String, Object>> products = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> product = new HashMap<>();
                product.put("id",          rs.getInt("id"));
                product.put("name",        rs.getString("name"));
                product.put("description", rs.getString("description"));
                product.put("price",       rs.getDouble("price"));
                product.put("category",    rs.getString("category"));
                product.put("image",       rs.getString("image"));
                product.put("stock",       rs.getInt("stock"));
                product.put("rating",      rs.getDouble("rating"));
                products.add(product);
            }

            request.setAttribute("products", products);
            request.setAttribute("category", category != null ? category : "");
            request.setAttribute("search",   search   != null ? search   : "");

            RequestDispatcher rd = request.getRequestDispatcher("products.jsp");
            rd.forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
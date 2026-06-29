// This file belongs to com.ecommerce package
package com.ecommerce;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@WebServlet("/CartServlet")
public class CartServlet extends HttpServlet {

    // ===== doPost handles ALL cart actions: add, remove, update, clear =====
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // Security check — must be logged in
        if (session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String action = request.getParameter("action");
        String productIdStr = request.getParameter("productId");

        // Routes to the correct private method based on which button was clicked
        switch (action == null ? "" : action) {

            case "add":
                addToCart(userId, Integer.parseInt(productIdStr), response);
                break;

            case "remove":
                removeFromCart(Integer.parseInt(request.getParameter("cartId")), response);
                break;

            case "update":
                updateQuantity(
                        Integer.parseInt(request.getParameter("cartId")),
                        Integer.parseInt(request.getParameter("quantity")),
                        response
                );
                break;

            case "clear":
                clearCart(userId, response);
                break;

            default:
                response.sendRedirect("CartServlet");
        }
    }

    // ===== doGet displays the cart page =====
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        if (session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (int) session.getAttribute("userId");

        try (Connection con = DBConnection.getConnection()) {

            // JOIN combines cart rows with their matching product details
            // in ONE query, instead of running a separate query per item
            // (avoids the "N+1 query problem" — a common performance bug)
            PreparedStatement ps = con.prepareStatement(
                    "SELECT c.id AS cartId, c.quantity, " +
                            "       p.id AS productId, p.name, p.price, p.image, p.stock " +
                            "FROM cart c " +
                            "JOIN products p ON c.product_id = p.id " +
                            "WHERE c.user_id = ? " +
                            "ORDER BY c.added_at DESC"
            );
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            List<Map<String, Object>> cartItems = new ArrayList<>();
            double grandTotal = 0;

            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("cartId",    rs.getInt("cartId"));
                item.put("quantity",  rs.getInt("quantity"));
                item.put("productId", rs.getInt("productId"));
                item.put("name",      rs.getString("name"));
                item.put("price",     rs.getDouble("price"));
                item.put("image",     rs.getString("image"));
                item.put("stock",     rs.getInt("stock"));

                double itemTotal = rs.getDouble("price") * rs.getInt("quantity");
                item.put("itemTotal", itemTotal);

                grandTotal += itemTotal;
                cartItems.add(item);
            }

            int totalItems = cartItems.stream()
                    .mapToInt(i -> (int) i.get("quantity"))
                    .sum();

            request.setAttribute("cartItems",  cartItems);
            request.setAttribute("grandTotal", grandTotal);
            request.setAttribute("totalItems", totalItems);

            RequestDispatcher rd = request.getRequestDispatcher("cart.jsp");
            rd.forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== ADD TO CART =====
    private void addToCart(int userId, int productId, HttpServletResponse response)
            throws IOException {

        // ONE connection used for this whole method, since we run multiple
        // related queries that should be treated as one logical operation
        try (Connection con = DBConnection.getConnection()) {

            // ===== Check stock availability BEFORE adding =====
            // INTERVIEW CONCEPT: this prevents a user from adding more items
            // to their cart than actually exist in stock.
            PreparedStatement stockCheck = con.prepareStatement(
                    "SELECT stock FROM products WHERE id = ?"
            );
            stockCheck.setInt(1, productId);
            ResultSet stockRs = stockCheck.executeQuery();
            int availableStock = stockRs.next() ? stockRs.getInt("stock") : 0;

            // ===== Check how many the user already has in their cart =====
            PreparedStatement check = con.prepareStatement(
                    "SELECT * FROM cart WHERE user_id = ? AND product_id = ?"
            );
            check.setInt(1, userId);
            check.setInt(2, productId);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                int currentQty = rs.getInt("quantity");

                // Don't allow adding more than what's in stock
                if (currentQty >= availableStock) {
                    response.sendRedirect("CartServlet?error=outofstock");
                    return;
                }

                PreparedStatement update = con.prepareStatement(
                        "UPDATE cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?"
                );
                update.setInt(1, userId);
                update.setInt(2, productId);
                update.executeUpdate();

            } else {
                if (availableStock <= 0) {
                    response.sendRedirect("CartServlet?error=outofstock");
                    return;
                }

                PreparedStatement insert = con.prepareStatement(
                        "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, 1)"
                );
                insert.setInt(1, userId);
                insert.setInt(2, productId);
                insert.executeUpdate();
            }

            response.sendRedirect("CartServlet");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== REMOVE FROM CART =====
    private void removeFromCart(int cartId, HttpServletResponse response) throws IOException {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM cart WHERE id = ?");
            ps.setInt(1, cartId);
            ps.executeUpdate();
            response.sendRedirect("CartServlet");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== UPDATE QUANTITY =====
    private void updateQuantity(int cartId, int quantity, HttpServletResponse response)
            throws IOException {
        try (Connection con = DBConnection.getConnection()) {

            // If quantity drops to 0 or below, just remove the item entirely
            if (quantity <= 0) {
                removeFromCart(cartId, response);
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE cart SET quantity = ? WHERE id = ?"
            );
            ps.setInt(1, quantity);
            ps.setInt(2, cartId);
            ps.executeUpdate();

            response.sendRedirect("CartServlet");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== CLEAR CART =====
    private void clearCart(int userId, HttpServletResponse response) throws IOException {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM cart WHERE user_id = ?");
            ps.setInt(1, userId);
            ps.executeUpdate();
            response.sendRedirect("CartServlet");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
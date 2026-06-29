// This file belongs to com.ecommerce package
package com.ecommerce;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AccountServlet")
public class AccountServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // Security check — must be logged in to view account page
        if (session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (int) session.getAttribute("userId");

        try (Connection con = DBConnection.getConnection()) {

            // ===== Get basic account info =====
            PreparedStatement userPs = con.prepareStatement(
                    "SELECT name, email, role, created_at FROM users WHERE id = ?"
            );
            userPs.setInt(1, userId);
            ResultSet userRs = userPs.executeQuery();

            if (userRs.next()) {
                request.setAttribute("accountName",  userRs.getString("name"));
                request.setAttribute("accountEmail", userRs.getString("email"));
                request.setAttribute("accountRole",  userRs.getString("role"));
                request.setAttribute("memberSince",  userRs.getTimestamp("created_at"));
            }

            // ===== Get preferences (age + interests) =====
            PreparedStatement prefPs = con.prepareStatement(
                    "SELECT age, interests FROM user_preferences WHERE user_id = ?"
            );
            prefPs.setInt(1, userId);
            ResultSet prefRs = prefPs.executeQuery();

            if (prefRs.next()) {
                request.setAttribute("accountAge", prefRs.getInt("age"));
                request.setAttribute("accountInterests", prefRs.getString("interests"));
            }

            // ===== Get order/cart stats — how many items currently in cart =====
            PreparedStatement cartCountPs = con.prepareStatement(
                    "SELECT COUNT(*) AS itemCount, COALESCE(SUM(quantity), 0) AS totalQty " +
                            "FROM cart WHERE user_id = ?"
            );
            cartCountPs.setInt(1, userId);
            ResultSet cartRs = cartCountPs.executeQuery();

            if (cartRs.next()) {
                request.setAttribute("cartItemCount", cartRs.getInt("itemCount"));
                request.setAttribute("cartTotalQty",  cartRs.getInt("totalQty"));
            }

            RequestDispatcher rd = request.getRequestDispatcher("account.jsp");
            rd.forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
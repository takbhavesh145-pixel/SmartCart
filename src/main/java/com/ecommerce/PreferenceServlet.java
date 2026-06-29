// This file belongs to com.ecommerce package
package com.ecommerce;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/PreferenceServlet")
public class PreferenceServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // Security check — must be logged in
        if (session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // ===== Validate age =====
        // INTERVIEW CONCEPT: never trust raw form input directly.
        // Integer.parseInt() will THROW an exception if the value isn't
        // a valid number (e.g. someone tampered with the form using dev tools).
        int age;
        try {
            age = Integer.parseInt(request.getParameter("age"));
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Please enter a valid age.");
            request.getRequestDispatcher("preference.jsp").forward(request, response);
            return;
        }

        if (age < 10 || age > 120) {
            request.setAttribute("error", "Age must be between 10 and 100.");
            request.getRequestDispatcher("preference.jsp").forward(request, response);
            return;
        }

        // ===== Validate interests =====
        // getParameterValues (plural) reads ALL checked checkboxes with the
        // same name="interests" attribute, returning an array
        String[] interestsArray = request.getParameterValues("interests");

        if (interestsArray == null || interestsArray.length == 0) {
            request.setAttribute("error", "Please select at least one interest.");
            request.getRequestDispatcher("preference.jsp").forward(request, response);
            return;
        }

        // Convert ["Electronics","Sports"] into "Electronics,Sports"
        // for storage in a single VARCHAR column
        String interests = String.join(",", interestsArray);

        int userId = (int) session.getAttribute("userId");

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO user_preferences (user_id, age, interests) VALUES (?, ?, ?)"
            );
            ps.setInt(1, userId);
            ps.setInt(2, age);
            ps.setString(3, interests);
            ps.executeUpdate();

            // Save in session too, so the very next page (products.jsp,
            // and later the chatbot) can use these values immediately
            // without querying the database again
            session.setAttribute("userAge", age);
            session.setAttribute("userInterests", interests);
            session.setAttribute("hasPreferences", true);

            System.out.println("Preferences saved for user: " + userId);

            response.sendRedirect("ProductServlet");

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Something went wrong. Please try again.");
            request.getRequestDispatcher("preference.jsp").forward(request, response);
        }
    }
}
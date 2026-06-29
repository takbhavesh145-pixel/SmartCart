// This file belongs to com.ecommerce package
package com.ecommerce;

import java.io.IOException;

// Servlet tools — same imports as your original
import jakarta.servlet.ServletException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

// BCrypt library — for hashing passwords (added via pom.xml dependency)
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name     = request.getParameter("name");
        String email    = request.getParameter("email");
        String password = request.getParameter("password");

        // ===== STEP 1: Server-side validation =====
        // INTERVIEW CONCEPT: NEVER trust the browser's "required" attribute alone.
        // Anyone can send a raw HTTP request bypassing your HTML form entirely
        // (using Postman, curl, etc). The SERVER must independently check everything.
        if (name == null || name.trim().length() < 2) {
            request.setAttribute("error", "Please enter a valid name.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        if (email == null || !email.contains("@")) {
            request.setAttribute("error", "Please enter a valid email.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        if (password == null || password.length() < 8) {
            request.setAttribute("error", "Password must be at least 8 characters.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        // try-with-resources: "con" is AUTOMATICALLY closed when this block ends,
        // even if an exception happens — no need for manual con.close()
        try (Connection con = DBConnection.getConnection()) {

            // ===== STEP 2: Check if email is already registered =====
            PreparedStatement check = con.prepareStatement(
                    "SELECT * FROM users WHERE email = ?"
            );
            check.setString(1, email.trim().toLowerCase());
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                // Email already exists — show error
                request.setAttribute("error", "Email already registered!");
                RequestDispatcher rd = request.getRequestDispatcher("register.jsp");
                rd.forward(request, response);

            } else {

                // ===== STEP 3: Hash the password BEFORE saving =====
                // This is the MOST IMPORTANT line in this whole file.
                // BCrypt.gensalt(12) creates a random "salt" so even if two users
                // pick the same password, their stored hashes look completely different.
                // We NEVER save the raw "password" variable to the database.
                String hashedPassword = BCrypt.hashpw(password,BCrypt.gensalt(12));

                // ===== STEP 4: Insert the new user =====
                // Statement.RETURN_GENERATED_KEYS lets us get back the new user's ID
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, 'user')",
                        // Tell JDBC:
                        // "After insertion, I want the generated primary key (id)"
                        Statement.RETURN_GENERATED_KEYS
                );

                ps.setString(1, name.trim());
                ps.setString(2, email.trim().toLowerCase());
                ps.setString(3, hashedPassword); // hashed, never plain text

                ps.executeUpdate();
                //used for INSERT, UPDATE, DELETE (changes data, returns count)
                System.out.println("New user registered: " + email);

                // sendRedirect (not forward) so refreshing the page doesn't
                // resubmit the form again
                response.sendRedirect("login.jsp?registered=true");
            }

        } catch (Exception e) {
            // Logs the FULL error so you can debug what went wrong
            e.printStackTrace();
            request.setAttribute("error", "Registration failed. Please try again.");
            try {
                request.getRequestDispatcher("register.jsp").forward(request, response);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
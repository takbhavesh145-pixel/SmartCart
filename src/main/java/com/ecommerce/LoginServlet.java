// This file belongs to com.ecommerce package
package com.ecommerce;
// java.io.IOException — handles input/output errors
// Thrown when response.sendRedirect() or forward() fails
// e.g. if the network connection to browser drops mid-response
import java.io.IOException;

// ServletException — handles Servlet-specific errors
// Thrown when something goes wrong inside doPost() itself
// Required in method signature: throws ServletException, IOException
import jakarta.servlet.ServletException;

// RequestDispatcher — used to FORWARD request to a JSP page
// forward() keeps the same request object (so attributes survive)
// Different from sendRedirect() which creates a brand new request
// Used here to send user back to login.jsp WITH the error message attached
import jakarta.servlet.RequestDispatcher;

// @WebServlet annotation — maps a URL to this Servlet
// Replaces the need to configure URL mappings in web.xml
// When browser hits /LoginServlet → Tomcat runs this class
import jakarta.servlet.annotation.WebServlet;

// HttpServlet — the parent class your Servlet extends
// Provides doGet(), doPost() methods you override
// Handles all low-level HTTP protocol details for you
import jakarta.servlet.http.HttpServlet;

// HttpServletRequest — represents everything the BROWSER sent to server
// Contains: form data, URL parameters, headers, cookies
// Used to read: email = request.getParameter("email")
import jakarta.servlet.http.HttpServletRequest;

// HttpServletResponse — represents what SERVER sends back to browser
// Used to: sendRedirect(), set content type, write response
import jakarta.servlet.http.HttpServletResponse;

// HttpSession — stores data about a specific logged-in user
// Lives on the SERVER side (not in browser)
// Survives across multiple requests from the same user
// Used to store: userId, userName, userRole after login
// INTERVIEW CONCEPT: HTTP is stateless — each request is independent
// Session is how we remember "who is this user" across requests
import jakarta.servlet.http.HttpSession;

// Connection — represents ONE borrowed TCP session with MySQL
// Borrowed from HikariCP pool, returned automatically via try-with-resources
import java.sql.Connection;

// PreparedStatement — safe way to write SQL queries with parameters
// The ? placeholders are filled safely — prevents SQL injection
// INTERVIEW CONCEPT: SQL injection = attacker types SQL into your form
// e.g. email field: "x' OR '1'='1" — without PreparedStatement this
// would manipulate your query. PreparedStatement treats it as plain text.
import java.sql.PreparedStatement;

// ResultSet — holds the rows returned by a SELECT query
// Like a cursor that moves through results row by row
// rs.next() moves to next row, returns false when no more rows
import java.sql.ResultSet;

// BCrypt — secure password verification library
// BCrypt.checkpw(typedPassword, storedHash) returns true/false
// INTERVIEW CONCEPT: we never store plain passwords in DB
// We store BCrypt hash — even if DB leaks, passwords are safe
// Login works by hashing the typed password and comparing hashes
// NOT by comparing plain text strings
import org.mindrot.jbcrypt.BCrypt;


@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email    = request.getParameter("email");
        String password = request.getParameter("password");

        try (Connection con = DBConnection.getConnection()) {

            // ===== STEP 1: Look up user by EMAIL ONLY =====
            // INTERVIEW CONCEPT: We do NOT put password in this SQL query anymore.
            // Your old code did "WHERE email=? AND password=?" — but since
            // passwords are now BCrypt hashes, that comparison would never
            // match correctly. The password check happens separately below,
            // in Java, using BCrypt.checkpw().
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM users WHERE email = ?"
            );
            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();

            // ===== STEP 2: Verify password with BCrypt =====
            // rs.next() moves to the first matching row, if any exists.
            // BCrypt.checkpw() re-hashes the typed password using the SAME
            // salt stored inside the database hash, then compares them safely.
            if (rs.next() && BCrypt.checkpw(password, rs.getString("password"))) {

                int userId      = rs.getInt("id");
                String userName = rs.getString("name");
                String userRole = rs.getString("role");

                // ===== STEP 3: Prevent session fixation =====
                // INTERVIEW CONCEPT: "session fixation" is an attack where
                // someone tricks a victim into using a KNOWN session ID,
                // then hijacks their account after they log in.
                // FIX: destroy any old session, then create a BRAND NEW one
                // at the exact moment of successful login.
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                HttpSession session = request.getSession(true);

                // ===== STEP 4: Store user info in the new session =====
                session.setAttribute("userId",   userId);
                session.setAttribute("userName", userName);
                session.setAttribute("userRole", userRole);

                // Auto-expire session after 30 minutes of inactivity
                session.setMaxInactiveInterval(30 * 60); // value is in SECONDS

                System.out.println("User logged in: " + email);

                // ===== STEP 5: Route based on role =====
                if ("admin".equals(userRole)) {
                    response.sendRedirect("admin/dashboard.jsp");
                    return;
                }

                // ===== STEP 6: Check if preferences already exist =====
                PreparedStatement prefCheck = con.prepareStatement(
                        "SELECT * FROM user_preferences WHERE user_id = ?"
                );
                prefCheck.setInt(1, userId);
                ResultSet prefRs = prefCheck.executeQuery();

                if (prefRs.next()) {
                    session.setAttribute("userInterests", prefRs.getString("interests"));
                    session.setAttribute("userAge", prefRs.getInt("age"));
                    session.setAttribute("hasPreferences", true);

                    response.sendRedirect("ProductServlet");
                } else {
                    // First time login — ask for preferences first
                    response.sendRedirect("preference.jsp");
                }

            } else {
                // ===== Wrong email OR wrong password =====
                // INTERVIEW CONCEPT — SECURITY: always show the SAME generic
                // error message whether the email doesn't exist or the password
                // is wrong. If you reveal which one was wrong, attackers can
                // use that to figure out which emails are registered
                // (called "user enumeration" — a real security flaw).
                request.setAttribute("error", "Invalid email or password.");
                RequestDispatcher rd = request.getRequestDispatcher("login.jsp");
                rd.forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
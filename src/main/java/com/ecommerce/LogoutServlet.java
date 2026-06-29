// This file belongs to com.ecommerce package
package com.ecommerce;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * LogoutServlet — your original project's "Logout" link just pointed
 * straight to login.jsp. That does NOT actually end the session —
 * the server keeps the session alive, so anyone who knows a protected
 * URL could still load it even after clicking "Logout".
 *
 * This Servlet properly destroys the session on the server side.
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // getSession(false) means: "give me the existing session if there
        // is one, but don't create a new empty one if there isn't"
        HttpSession session = request.getSession(false);

        if (session != null) {
            String userName = String.valueOf(session.getAttribute("userName"));

            // invalidate() destroys ALL data tied to this session on the
            // SERVER side. Even if the browser still has the old session
            // cookie, it now points to nothing.
            session.invalidate();

            System.out.println("User logged out: " + userName);
        }

        // Also clear the session cookie on the browser side, for good measure
        // constrctor(string name , string value)
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setMaxAge(0);       // expires immediately
        cookie.setHttpOnly(true);  // not accessible via JavaScript (XSS protection)
        cookie.setPath(request.getContextPath());
        response.addCookie(cookie);

        response.sendRedirect("login.jsp");
    }
}
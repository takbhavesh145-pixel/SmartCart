// This file belongs to com.ecommerce package
package com.ecommerce;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * AdminFilter — blocks any non-admin user from reaching admin pages.
 *
 * INTERVIEW CONCEPT: this is a SECOND layer of access control, separate
 * from regular login. Being logged in is not enough here — the session's
 * "userRole" must specifically equal "admin". This is exactly how
 * real systems separate "authentication" (are you logged in?) from
 * "authorization" (are you ALLOWED to do this specific thing?).
 *
 * urlPatterns="/admin/*" means this filter runs for EVERY URL that
 * starts with /admin/ — so any new admin page we add later is
 * automatically protected without writing any extra code.
 */
@WebFilter(urlPatterns = "/admin/*")
public class AdminFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req = (HttpServletRequest)  request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);

        boolean isLoggedIn = (session != null && session.getAttribute("userId") != null);
        boolean isAdmin    = (isLoggedIn && "admin".equals(session.getAttribute("userRole")));

        if (isAdmin) {
            // Allowed — let the request continue to the admin page
            chain.doFilter(request, response);

        } else if (isLoggedIn) {
            // Logged in, but NOT an admin — block with a clear message
            // instead of silently redirecting, since this is a deliberate
            // access violation, not just "please log in"
            res.sendRedirect(req.getContextPath() + "/ProductServlet?error=accessdenied");

        } else {
            // Not logged in at all
            res.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }
}
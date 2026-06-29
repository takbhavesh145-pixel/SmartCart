<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%
    // Entry point of the whole site — routes based on login status
    if (session.getAttribute("userId") != null) {
        response.sendRedirect("ProductServlet");
    } else {
        response.sendRedirect("login.jsp");
    }
%>
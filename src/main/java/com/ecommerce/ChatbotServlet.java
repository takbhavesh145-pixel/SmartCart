// This file belongs to com.ecommerce package
package com.ecommerce;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ChatbotServlet")
public class ChatbotServlet extends HttpServlet {

    // List of valid categories — used to detect which category the user
    // is asking about, by checking if their message CONTAINS any of these
    private static final String[] CATEGORIES = {
            "Electronics", "Fashion", "Sports", "Books", "Beauty", "Home & Kitchen"
    };

    // Regex pattern to find a price limit in the user's message.
    // Matches phrases like "under 5000", "below 2000", "less than 1000"
    // INTERVIEW CONCEPT: regex (regular expressions) let you search text
    // for PATTERNS instead of exact matches. (?i) makes it case-insensitive.
    private static final Pattern PRICE_PATTERN =
            Pattern.compile("(?i)(under|below|less than)\\s+(\\d+)");

    // Common conversational words — used to tell the difference between
    // "iphone" (a product search) and "hi" / "thanks" (just chatting)
    private static final String[] COMMON_WORDS = {
            "how", "are", "you", "hi", "hello", "hey",
            "thanks", "thank", "ok", "okay", "yes", "no", "what", "can"
    };

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // Security check — must be logged in to use the chatbot
        if (session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Read the user's typed message
        String userMessage = request.getParameter("message");

        if (userMessage == null || userMessage.trim().isEmpty()) {
            sendJsonResponse(response, "Please type something for me to help with!");
            return;
        }

        // Work with a lowercase copy for matching, but keep the original
        // for anything we might want to display back
        String lowerMessage = userMessage.toLowerCase();

        // ===== STEP 1: Detect if user mentioned a category =====
        String detectedCategory = null;
        for (String category : CATEGORIES) {
            if (lowerMessage.contains(category.toLowerCase())) {
                detectedCategory = category;
                break;
            }
        }

        // ===== STEP 2: Detect if user mentioned a price limit =====
        Integer maxPrice = null;
        Matcher priceMatcher = PRICE_PATTERN.matcher(lowerMessage);
        if (priceMatcher.find()) {
            // group(2) is the number captured by the (\\d+) part of the regex
            maxPrice = Integer.parseInt(priceMatcher.group(2));
        }

        // ===== STEP 2.5: Detect if the message looks like a product name search =====
        // If we didn't find a category AND the message is short (1-4 words,
        // like "iphone" or "running shoes"), treat it as a product name
        // search instead of a conversational message like "how are you".
        String searchKeyword = null;
        if (detectedCategory == null) {
            String[] words = userMessage.trim().split("\\s+");

            boolean looksConversational = false;
            for (String word : words) {
                for (String common : COMMON_WORDS) {
                    if (word.toLowerCase().equals(common)) {
                        looksConversational = true;
                        break;
                    }
                }
            }

            if (words.length <= 4 && !looksConversational) {
                searchKeyword = userMessage.trim();
            }
        }

        // ===== STEP 3: If no category mentioned, fall back to the user's
        // saved interests from preference.jsp (personalization!) =====
        if (detectedCategory == null && lowerMessage.contains("recommend")) {
            String interests = (String) session.getAttribute("userInterests");
            if (interests != null && !interests.isEmpty()) {
                // Just take the FIRST interest from their comma-separated list
                detectedCategory = interests.split(",")[0];
                // A category match means this is no longer a product-name search
                searchKeyword = null;
            }
        }

        // Did we understand the message in ANY way?
        boolean understoodSomething =
                (detectedCategory != null || maxPrice != null || searchKeyword != null);

        // ===== STEP 4: Query the database — ONLY if we understood something =====
        // BUG FIX: previously this query ran for EVERY message, even ones
        // we didn't understand (like "how are you"), which made the bot
        // always show top products no matter what was typed.
        List<String> productNames = new ArrayList<>();

        if (understoodSomething) {
            try (Connection con = DBConnection.getConnection()) {

                StringBuilder sql = new StringBuilder(
                        "SELECT name, price FROM products WHERE stock > 0"
                );

                // Build the query dynamically based on what filters apply
                if (detectedCategory != null) {
                    sql.append(" AND category = ?");
                }
                if (maxPrice != null) {
                    sql.append(" AND price <= ?");
                }
                if (searchKeyword != null) {
                    // LIKE with % wildcards finds the keyword ANYWHERE in
                    // the product name, e.g. "iphone" matches "iPhone 15 Pro"
                    sql.append(" AND name LIKE ?");
                }
                sql.append(" ORDER BY rating DESC LIMIT 5");

                PreparedStatement ps = con.prepareStatement(sql.toString());

                // Fill in the ? placeholders IN THE SAME ORDER they appear above
                int paramIndex = 1;
                if (detectedCategory != null) {
                    ps.setString(paramIndex++, detectedCategory);
                }
                if (maxPrice != null) {
                    ps.setInt(paramIndex++, maxPrice);
                }
                if (searchKeyword != null) {
                    ps.setString(paramIndex++, "%" + searchKeyword + "%");
                }

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    productNames.add(rs.getString("name") + " (₹" + rs.getInt("price") + ")");
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(response, "Sorry, something went wrong. Please try again.");
                return;
            }
        }

        // ===== STEP 5: Build a reply message based on what we found =====
        String reply;

        if (!productNames.isEmpty()) {
            StringBuilder replyBuilder = new StringBuilder();

            if (detectedCategory != null && maxPrice != null) {
                replyBuilder.append("Here are some ").append(detectedCategory)
                        .append(" items under ₹").append(maxPrice).append(":\n");
            } else if (detectedCategory != null) {
                replyBuilder.append("Here are some great ").append(detectedCategory)
                        .append(" picks for you:\n");
            } else if (searchKeyword != null) {
                replyBuilder.append("Here's what I found for \"")
                        .append(searchKeyword).append("\":\n");
            } else {
                replyBuilder.append("Here are some top-rated products you might like:\n");
            }

            for (String name : productNames) {
                replyBuilder.append("• ").append(name).append("\n");
            }

            reply = replyBuilder.toString();

        } else if (understoodSomething) {
            // We understood the filters/search, but no products matched
            reply = "I couldn't find anything matching that. Try browsing all "
                    + (detectedCategory != null ? detectedCategory : "products") + "!";

        } else {
            // We genuinely didn't understand the message — give a helpful hint
            reply = "I can help you find products! Try asking things like:\n"
                    + "• \"Show me Electronics\"\n"
                    + "• \"Books under 500\"\n"
                    + "• \"iPhone\" (search by product name)\n"
                    + "• \"Recommend something for me\"";
        }

        sendJsonResponse(response, reply);
    }

    /**
     * Sends a JSON response back to the chatbot's JavaScript on the frontend.
     * INTERVIEW CONCEPT: this is a basic example of building an API endpoint
     * that returns JSON instead of forwarding to a JSP — the frontend JS
     * reads this JSON and updates the chat window dynamically without
     * reloading the page (this is the foundation of "AJAX").
     */
    private void sendJsonResponse(HttpServletResponse response, String reply)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Manually building JSON here (no external library) — fine for a
        // simple structure like this. For complex objects, a library like
        // Gson or Jackson would be used instead.
        StringBuilder json = new StringBuilder();
        json.append("{\"reply\": \"").append(escapeJson(reply)).append("\"}");

        PrintWriter out = response.getWriter();
        out.write(json.toString());
        out.flush();
    }

    /** Escapes characters that would break JSON formatting (quotes, newlines) */
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
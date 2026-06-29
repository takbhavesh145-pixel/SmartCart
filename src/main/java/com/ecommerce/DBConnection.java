// This file belongs to com.ecommerce package
package com.ecommerce;

// HikariConfig — settings object for the connection pool
// You fill this with DB url, username, password, pool size etc.
import com.zaxxer.hikari.HikariConfig;

// HikariDataSource — the actual pool object
// This holds all 10 open TCP connections to MySQL permanently
import com.zaxxer.hikari.HikariDataSource;

// Connection — represents ONE borrowed connection from the pool
// This is what every Servlet uses to run SQL queries
import java.sql.Connection;
import java.sql.SQLException;

// Properties — used to read key=value pairs from db.properties file
import java.util.Properties;

/**
 * DBConnection — manages database connections for the entire application.
 *
 * PATTERN USED: Bill Pugh Singleton + HikariCP Connection Pool
 *
 * BILL PUGH SINGLETON:
 * Ensures only ONE HikariDataSource object exists for the whole app.
 * Uses a nested static Holder class so the pool is created LAZILY —
 * only when getConnection() is first called, not when the class loads.
 * Thread-safe without synchronized keyword — JVM guarantees class
 * loading happens only once even if 100 threads call getConnection()
 * simultaneously.
 *
 * HIKARICP CONNECTION POOL:
 * Instead of opening a new TCP connection to MySQL on every request
 * (your old DBConnection did this — slow, crashes under load),
 * HikariCP opens 10 connections ONCE at startup and keeps them alive.
 * Each request BORROWS one connection, uses it, returns it to pool.
 * No new TCP handshake per request — much faster, handles concurrent users.
 */
public class DBConnection {

    // Private constructor — prevents anyone from doing "new DBConnection()"
    // This is the FIRST rule of any singleton pattern.
    // DBConnection is a utility class — only static methods, never instantiated.
    private DBConnection() {}


    // =========================================================
    // BILL PUGH HOLDER — the core of this pattern
    // =========================================================
    // This nested class is SEPARATE from DBConnection.
    // JVM does NOT load this class when DBConnection loads.
    // JVM ONLY loads Holder when something inside it is accessed
    // for the very first time — which happens when getConnection()
    // is called for the first time.
    //
    // After that first load:
    //   - DATA_SOURCE exists permanently
    //   - Holder never loads again
    //   - createPool() never runs again
    //   - Every future getConnection() call uses the same DATA_SOURCE
    private static class Holder {
        // "final" — this reference can never point to a different object
        // createPool() runs ONCE when Holder loads, never again
        private static final HikariDataSource DATA_SOURCE = createPool();
    }

    // =========================================================
    // createPool() — called exactly ONCE by Holder when it loads
    // =========================================================
    // Separated into its own method to keep Holder class clean.
    // Returns the fully configured HikariDataSource (the pool).
    private static HikariDataSource createPool() {
        try {
            // Explicitly load the MySQL JDBC driver class.
            // INTERVIEW CONCEPT: JDBC (Java Database Connectivity) is Java's
            // standard API for talking to relational databases.
            // The driver is the MySQL-specific implementation of that API —
            // it translates Java method calls into MySQL's wire protocol.
            // Without this, HikariCP cannot find the driver and throws
            // "No suitable driver" error (which we saw earlier in this project).
            Class.forName("com.mysql.cj.jdbc.Driver");


            // Create an empty config object — like a blank settings form
            HikariConfig config = new HikariConfig();

            // ===== Read credentials from db.properties =====
            // SECURITY: we never hardcode the password in this .java file.
            // db.properties sits in src/main/resources/ and is listed in
            // .gitignore so it never gets pushed to GitHub.
            // getClassLoader().getResourceAsStream() finds the file on the
            // classpath — Maven copies resources/ contents to classpath automatically.
            Properties props = new Properties();
            props.load(DBConnection.class.getClassLoader()
                    .getResourceAsStream("db.properties"));

            // Apply credentials to config
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));

            // ===== Pool sizing — this is what handles multiple threads =====
            // INTERVIEW CONCEPT: Tomcat creates a new Thread for every HTTP
            // request. If 50 users click "Add to Cart" simultaneously, that's
            // 50 threads all needing a DB connection at the same time.
            // Pool size controls how many can run concurrently vs wait.

            // Maximum 10 connections open at once.
            // Threads 1-10: each gets their own connection immediately
            // Threads 11+: wait briefly until one of the 10 is returned
            config.setMaximumPoolSize(10);

            // Keep at least 3 connections alive even when site is idle.
            // So the first user after a quiet period doesn't wait for
            // a fresh connection to be opened.
            config.setMinimumIdle(3);

            // If all 10 connections are busy and none frees up within
            // 30 seconds, throw an exception instead of waiting forever.
            // Prevents requests from hanging indefinitely under extreme load.
            config.setConnectionTimeout(30000); // milliseconds

            // Friendly name for this pool — shows up in logs
            config.setPoolName("SmartCartPool");

            // Build the pool — this opens the initial TCP connections to MySQL
            // and keeps them alive. This is the expensive step that only
            HikariDataSource dataSource = new HikariDataSource(config);
            return dataSource;

        } catch (Exception e) {
            System.out.println("DB Pool creation failed: " + e.getMessage());

            // Throw RuntimeException to stop the app from starting in a
            // broken state. Better to fail loudly at startup than to
            // silently fail on every user request later.
            throw new RuntimeException("Failed to create DB connection pool", e);
        }
    }

    // =========================================================
    // getConnection() — the ONE method every Servlet calls
    // =========================================================
    /**
     * Borrows ONE connection from the pool for a single request.
     *
     * HOW IT WORKS:
     * First call  → Holder loads → DATA_SOURCE created → connection borrowed
     * Every call after → Holder already loaded → same DATA_SOURCE → connection borrowed
     *
     * ALWAYS use inside try-with-resources:
     *   try (Connection con = DBConnection.getConnection()) {
     *       // use con to run SQL
     *   } // ← connection automatically returned to pool here
     *     // even if an exception was thrown inside the block
     *
     * "returned to pool" does NOT mean the TCP connection is closed —
     * it just means the Connection object is handed back so the next
     * thread can borrow it. The underlying TCP line stays open.
     */
    public static Connection getConnection() throws SQLException {
        // Holder.DATA_SOURCE triggers Holder class load on first call
        // Returns a borrowed Connection from the pool
        return Holder.DATA_SOURCE.getConnection();
    }
}
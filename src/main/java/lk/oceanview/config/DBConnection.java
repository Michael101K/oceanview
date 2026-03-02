package lk.oceanview.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Singleton Pattern
 * 
 * Ensures only ONE database connection instance exists 
 * throughout the application lifecycle.
 * This is a Design Pattern implementation (Singleton) required by Task B.
 */
public class DBConnection {

    // ------------------------------------------------
    // Database credentials - update if needed
    // ------------------------------------------------
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/ocean_view_resort?useSSL=false&serverTimezone=UTC";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "your_password_here"; // <-- change this

    // The single instance
    private static DBConnection instance;
    private Connection connection;

    // ------------------------------------------------
    // Private constructor - prevents direct instantiation
    // ------------------------------------------------
    private DBConnection() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("[DBConnection] Database connected successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection failed: " + e.getMessage());
        }
    }

    // ------------------------------------------------
    // Public method to get the single instance
    // ------------------------------------------------
    public static synchronized DBConnection getInstance() {
        if (instance == null || isConnectionClosed()) {
            instance = new DBConnection();
        }
        return instance;
    }

    // ------------------------------------------------
    // Returns the active connection
    // ------------------------------------------------
    public Connection getConnection() {
        return connection;
    }

    // ------------------------------------------------
    // Check if connection is closed/null
    // ------------------------------------------------
    private static boolean isConnectionClosed() {
        try {
            return instance.connection == null || instance.connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    // ------------------------------------------------
    // Close the connection (call on app shutdown)
    // ------------------------------------------------
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DBConnection] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DBConnection] Error closing connection: " + e.getMessage());
        }
    }
}
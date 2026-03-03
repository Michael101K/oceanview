package lk.oceanview.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Singleton Pattern
 *
 * The Singleton ensures only ONE instance of this class exists.
 * getConnection() returns a fresh valid connection every time it is called,
 * automatically handling reconnection if the previous connection was closed.
 */
public class DBConnection {

    // ------------------------------------------------
    // Database credentials - update password if needed
    // ------------------------------------------------
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/ocean_view_resort?useSSL=false&serverTimezone=UTC&autoReconnect=true";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "password@123"; // <-- change this

    // The single instance (Singleton)
    private static DBConnection instance;

    // ------------------------------------------------
    // Private constructor - loads the JDBC driver once
    // ------------------------------------------------
    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DBConnection] MySQL Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL Driver not found: " + e.getMessage());
        }
    }

    // ------------------------------------------------
    // Returns the single instance of DBConnection
    // ------------------------------------------------
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    // ------------------------------------------------
    // Returns a fresh connection every time it is called
    // ------------------------------------------------
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException e) {
            System.err.println("[DBConnection] Failed to get connection: " + e.getMessage());
            return null;
        }
    }
}
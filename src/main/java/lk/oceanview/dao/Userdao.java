package lk.oceanview.dao;

import lk.oceanview.config.DBConnection;
import lk.oceanview.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO - Data Access Object Pattern
 * Handles all database operations related to the 'users' table.
 */
public class UserDAO {


    // ------------------------------------------------
    // Authenticate user login (username + hashed password)
    // ------------------------------------------------
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = SHA2(?, 256) AND is_active = TRUE";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
        } catch (SQLException e) {
            System.err.println("[UserDAO] authenticate error: " + e.getMessage());
        }
        return null; // returns null if login fails
    }

    // ------------------------------------------------
    // Get all users (Admin only)
    // ------------------------------------------------
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY full_name";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getAllUsers error: " + e.getMessage());
        }
        return users;
    }

    // ------------------------------------------------
    // Get user by ID
    // ------------------------------------------------
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getUserById error: " + e.getMessage());
        }
        return null;
    }

    // ------------------------------------------------
    // Add new user (Admin only)
    // ------------------------------------------------
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, role, email) " +
                     "VALUES (?, SHA2(?, 256), ?, ?, ?)";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getEmail());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] addUser error: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Update user details
    // ------------------------------------------------
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET full_name = ?, role = ?, email = ?, is_active = ? " +
                     "WHERE user_id = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getRole());
            ps.setString(3, user.getEmail());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] updateUser error: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Map ResultSet row to User object
    // ------------------------------------------------
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setEmail(rs.getString("email"));
        user.setActive(rs.getBoolean("is_active"));
        return user;
    }
}
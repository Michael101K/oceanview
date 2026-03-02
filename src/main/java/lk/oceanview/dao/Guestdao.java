package lk.oceanview.dao;

import lk.oceanview.config.DBConnection;
import lk.oceanview.model.Guest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * GuestDAO - Data Access Object Pattern
 * Handles all database operations related to the 'guests' table.
 */
public class GuestDAO {

    private Connection connection;

    public GuestDAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }

    // ------------------------------------------------
    // Add a new guest and return the generated guest_id
    // ------------------------------------------------
    public int addGuest(Guest guest) {
        String sql = "INSERT INTO guests (full_name, address, contact_number, email, nic_number) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, guest.getFullName());
            ps.setString(2, guest.getAddress());
            ps.setString(3, guest.getContactNumber());
            ps.setString(4, guest.getEmail());
            ps.setString(5, guest.getNicNumber());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getInt(1); // return new guest_id
                }
            }
        } catch (SQLException e) {
            System.err.println("[GuestDAO] addGuest error: " + e.getMessage());
        }
        return -1; // failed
    }

    // ------------------------------------------------
    // Get guest by ID
    // ------------------------------------------------
    public Guest getGuestById(int guestId) {
        String sql = "SELECT * FROM guests WHERE guest_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToGuest(rs);
            }
        } catch (SQLException e) {
            System.err.println("[GuestDAO] getGuestById error: " + e.getMessage());
        }
        return null;
    }

    // ------------------------------------------------
    // Get guest by NIC number (check if guest already exists)
    // ------------------------------------------------
    public Guest getGuestByNIC(String nicNumber) {
        String sql = "SELECT * FROM guests WHERE nic_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nicNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToGuest(rs);
            }
        } catch (SQLException e) {
            System.err.println("[GuestDAO] getGuestByNIC error: " + e.getMessage());
        }
        return null;
    }

    // ------------------------------------------------
    // Search guests by name
    // ------------------------------------------------
    public List<Guest> searchGuestsByName(String name) {
        List<Guest> guests = new ArrayList<>();
        String sql = "SELECT * FROM guests WHERE full_name LIKE ? ORDER BY full_name";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                guests.add(mapResultSetToGuest(rs));
            }
        } catch (SQLException e) {
            System.err.println("[GuestDAO] searchGuestsByName error: " + e.getMessage());
        }
        return guests;
    }

    // ------------------------------------------------
    // Get all guests
    // ------------------------------------------------
    public List<Guest> getAllGuests() {
        List<Guest> guests = new ArrayList<>();
        String sql = "SELECT * FROM guests ORDER BY full_name";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                guests.add(mapResultSetToGuest(rs));
            }
        } catch (SQLException e) {
            System.err.println("[GuestDAO] getAllGuests error: " + e.getMessage());
        }
        return guests;
    }

    // ------------------------------------------------
    // Update guest details
    // ------------------------------------------------
    public boolean updateGuest(Guest guest) {
        String sql = "UPDATE guests SET full_name = ?, address = ?, contact_number = ?, " +
                     "email = ?, nic_number = ? WHERE guest_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, guest.getFullName());
            ps.setString(2, guest.getAddress());
            ps.setString(3, guest.getContactNumber());
            ps.setString(4, guest.getEmail());
            ps.setString(5, guest.getNicNumber());
            ps.setInt(6, guest.getGuestId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[GuestDAO] updateGuest error: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Map ResultSet row to Guest object
    // ------------------------------------------------
    private Guest mapResultSetToGuest(ResultSet rs) throws SQLException {
        Guest guest = new Guest();
        guest.setGuestId(rs.getInt("guest_id"));
        guest.setFullName(rs.getString("full_name"));
        guest.setAddress(rs.getString("address"));
        guest.setContactNumber(rs.getString("contact_number"));
        guest.setEmail(rs.getString("email"));
        guest.setNicNumber(rs.getString("nic_number"));
        return guest;
    }
}
package lk.oceanview.dao;

import lk.oceanview.config.DBConnection;
import lk.oceanview.model.Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ReservationDAO - Data Access Object Pattern
 * Handles all database operations related to the 'reservations' table.
 */
public class ReservationDAO {

    // ------------------------------------------------
    // Add a new reservation - returns generated reservation_id
    // ------------------------------------------------
    public int addReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (reservation_number, guest_id, room_id, " +
                     "check_in_date, check_out_date, total_amount, status, special_requests, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, reservation.getReservationNumber());
            ps.setInt(2, reservation.getGuestId());
            ps.setInt(3, reservation.getRoomId());
            ps.setDate(4, java.sql.Date.valueOf(reservation.getCheckInDate()));
            ps.setDate(5, java.sql.Date.valueOf(reservation.getCheckOutDate()));
            ps.setDouble(6, reservation.getTotalAmount());
            ps.setString(7, reservation.getStatus());
            ps.setString(8, reservation.getSpecialRequests());
            ps.setInt(9, reservation.getCreatedBy());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getInt(1); // return new reservation_id
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReservationDAO] addReservation error: " + e.getMessage());
        }
        return -1; // failed
    }

    // ------------------------------------------------
    // Get reservation by reservation number (e.g. OVR-2025-0001)
    // ------------------------------------------------
    public Reservation getReservationByNumber(String reservationNumber) {
        String sql = "SELECT * FROM vw_reservation_details WHERE reservation_number = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, reservationNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToReservation(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ReservationDAO] getReservationByNumber error: " + e.getMessage());
        }
        return null;
    }

    // ------------------------------------------------
    // Get reservation by ID
    // ------------------------------------------------
    public Reservation getReservationById(int reservationId) {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapBasicReservation(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ReservationDAO] getReservationById error: " + e.getMessage());
        }
        return null;
    }

    // ------------------------------------------------
    // Get all reservations
    // ------------------------------------------------
    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM vw_reservation_details ORDER BY created_at DESC";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ReservationDAO] getAllReservations error: " + e.getMessage());
        }
        return reservations;
    }

    // ------------------------------------------------
    // Get reservations by status (e.g. CONFIRMED, CHECKED_IN)
    // ------------------------------------------------
    public List<Reservation> getReservationsByStatus(String status) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM vw_reservation_details WHERE status = ? ORDER BY check_in_date";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ReservationDAO] getReservationsByStatus error: " + e.getMessage());
        }
        return reservations;
    }

    // ------------------------------------------------
    // Update reservation status
    // ------------------------------------------------
    public boolean updateReservationStatus(int reservationId, String status) {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ReservationDAO] updateReservationStatus error: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Cancel reservation
    // ------------------------------------------------
    public boolean cancelReservation(int reservationId) {
        return updateReservationStatus(reservationId, "CANCELLED");
    }

    // ------------------------------------------------
    // Generate next reservation number e.g. OVR-2025-0001
    // ------------------------------------------------
    public String generateReservationNumber() {
        String sql = "SELECT COUNT(*) FROM reservations WHERE YEAR(created_at) = YEAR(NOW())";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                int year  = LocalDate.now().getYear();
                return String.format("OVR-%d-%04d", year, count);
            }
        } catch (SQLException e) {
            System.err.println("[ReservationDAO] generateReservationNumber error: " + e.getMessage());
        }
        return "OVR-" + LocalDate.now().getYear() + "-0001";
    }

    // ------------------------------------------------
    // Get today's check-ins
    // ------------------------------------------------
    public List<Reservation> getTodayCheckIns() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM vw_reservation_details WHERE check_in_date = CURDATE() AND status = 'CONFIRMED'";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ReservationDAO] getTodayCheckIns error: " + e.getMessage());
        }
        return reservations;
    }

    // ------------------------------------------------
    // Map from vw_reservation_details view
    // ------------------------------------------------
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationNumber(rs.getString("reservation_number"));
        r.setGuestName(rs.getString("guest_name"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomTypeName(rs.getString("room_type"));
        r.setRatePerNight(rs.getDouble("rate_per_night"));
        r.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        r.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        r.setTotalAmount(rs.getDouble("total_amount"));
        r.setStatus(rs.getString("status"));
        r.setSpecialRequests(rs.getString("special_requests"));
        return r;
    }

    // ------------------------------------------------
    // Map from reservations table (basic)
    // ------------------------------------------------
    private Reservation mapBasicReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getInt("reservation_id"));
        r.setReservationNumber(rs.getString("reservation_number"));
        r.setGuestId(rs.getInt("guest_id"));
        r.setRoomId(rs.getInt("room_id"));
        r.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        r.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        r.setTotalAmount(rs.getDouble("total_amount"));
        r.setStatus(rs.getString("status"));
        r.setSpecialRequests(rs.getString("special_requests"));
        r.setCreatedBy(rs.getInt("created_by"));
        return r;
    }
}
package lk.oceanview.dao;

import lk.oceanview.config.DBConnection;
import lk.oceanview.model.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * BillDAO - Data Access Object Pattern
 * Handles all database operations related to the 'bills' table.
 */
public class BillDAO {

    private Connection connection;

    public BillDAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }

    // ------------------------------------------------
    // Generate and save a bill for a reservation
    // ------------------------------------------------
    public int addBill(Bill bill) {
        String sql = "INSERT INTO bills (reservation_id, room_charges, tax_amount, " +
                     "discount_amount, total_amount, payment_status, payment_method) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bill.getReservationId());
            ps.setDouble(2, bill.getRoomCharges());
            ps.setDouble(3, bill.getTaxAmount());
            ps.setDouble(4, bill.getDiscountAmount());
            ps.setDouble(5, bill.getTotalAmount());
            ps.setString(6, bill.getPaymentStatus());
            ps.setString(7, bill.getPaymentMethod());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getInt(1); // return new bill_id
                }
            }
        } catch (SQLException e) {
            System.err.println("[BillDAO] addBill error: " + e.getMessage());
        }
        return -1;
    }

    // ------------------------------------------------
    // Get bill by reservation ID
    // ------------------------------------------------
    public Bill getBillByReservationId(int reservationId) {
        String sql = "SELECT b.*, r.reservation_number, g.full_name AS guest_name, " +
                     "r.num_nights, rt.rate_per_night " +
                     "FROM bills b " +
                     "JOIN reservations r  ON b.reservation_id = r.reservation_id " +
                     "JOIN guests g        ON r.guest_id = g.guest_id " +
                     "JOIN rooms rm        ON r.room_id = rm.room_id " +
                     "JOIN room_types rt   ON rm.room_type_id = rt.room_type_id " +
                     "WHERE b.reservation_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToBill(rs);
            }
        } catch (SQLException e) {
            System.err.println("[BillDAO] getBillByReservationId error: " + e.getMessage());
        }
        return null;
    }

    // ------------------------------------------------
    // Update payment status
    // ------------------------------------------------
    public boolean updatePaymentStatus(int billId, String paymentStatus) {
        String sql = "UPDATE bills SET payment_status = ? WHERE bill_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setInt(2, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BillDAO] updatePaymentStatus error: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Check if bill already exists for a reservation
    // ------------------------------------------------
    public boolean billExistsForReservation(int reservationId) {
        String sql = "SELECT COUNT(*) FROM bills WHERE reservation_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[BillDAO] billExistsForReservation error: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Map ResultSet to Bill object
    // ------------------------------------------------
    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setBillId(rs.getInt("bill_id"));
        bill.setReservationId(rs.getInt("reservation_id"));
        bill.setRoomCharges(rs.getDouble("room_charges"));
        bill.setTaxAmount(rs.getDouble("tax_amount"));
        bill.setDiscountAmount(rs.getDouble("discount_amount"));
        bill.setTotalAmount(rs.getDouble("total_amount"));
        bill.setPaymentStatus(rs.getString("payment_status"));
        bill.setPaymentMethod(rs.getString("payment_method"));
        bill.setReservationNumber(rs.getString("reservation_number"));
        bill.setGuestName(rs.getString("guest_name"));
        bill.setNumNights(rs.getLong("num_nights"));
        bill.setRatePerNight(rs.getDouble("rate_per_night"));
        return bill;
    }
}
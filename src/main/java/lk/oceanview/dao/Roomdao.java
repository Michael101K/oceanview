package lk.oceanview.dao;

import lk.oceanview.config.DBConnection;
import lk.oceanview.model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * RoomDAO - Data Access Object Pattern
 * Handles all database operations related to the 'rooms' table.
 */
public class RoomDAO {

    // ------------------------------------------------
    // Get all available rooms
    // ------------------------------------------------
    public List<Room> getAvailableRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM vw_available_rooms ORDER BY floor, room_number";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Room room = new Room();
                room.setRoomId(rs.getInt("room_id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setRoomTypeName(rs.getString("room_type"));
                room.setRatePerNight(rs.getDouble("rate_per_night"));
                room.setMaxOccupancy(rs.getInt("max_occupancy"));
                room.setFloor(rs.getInt("floor"));
                room.setStatus("AVAILABLE");
                rooms.add(room);
            }
        } catch (SQLException e) {
            System.err.println("[RoomDAO] getAvailableRooms error: " + e.getMessage());
        }
        return rooms;
    }

    // ------------------------------------------------
    // Get all rooms
    // ------------------------------------------------
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.room_id, r.room_number, r.floor, r.status, " +
                     "rt.type_name, rt.rate_per_night, rt.max_occupancy " +
                     "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.room_type_id " +
                     "ORDER BY r.floor, r.room_number";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        } catch (SQLException e) {
            System.err.println("[RoomDAO] getAllRooms error: " + e.getMessage());
        }
        return rooms;
    }

    // ------------------------------------------------
    // Get room by ID
    // ------------------------------------------------
    public Room getRoomById(int roomId) {
        String sql = "SELECT r.room_id, r.room_number, r.floor, r.status, " +
                     "rt.type_name, rt.rate_per_night, rt.max_occupancy " +
                     "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.room_type_id " +
                     "WHERE r.room_id = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToRoom(rs);
            }
        } catch (SQLException e) {
            System.err.println("[RoomDAO] getRoomById error: " + e.getMessage());
        }
        return null;
    }

    // ------------------------------------------------
    // Update room status (AVAILABLE / OCCUPIED / MAINTENANCE)
    // ------------------------------------------------
    public boolean updateRoomStatus(int roomId, String status) {
        String sql = "UPDATE rooms SET status = ? WHERE room_id = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RoomDAO] updateRoomStatus error: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Check if a room is available for given dates
    // ------------------------------------------------
    public boolean isRoomAvailableForDates(int roomId, String checkIn, String checkOut) {
        String sql = "SELECT COUNT(*) FROM reservations " +
                     "WHERE room_id = ? AND status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
                     "AND NOT (check_out_date <= ? OR check_in_date >= ?)";
        try (Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setString(2, checkIn);
            ps.setString(3, checkOut);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0; // 0 conflicts = available
            }
        } catch (SQLException e) {
            System.err.println("[RoomDAO] isRoomAvailableForDates error: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Map ResultSet row to Room object
    // ------------------------------------------------
    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomTypeName(rs.getString("type_name"));
        room.setRatePerNight(rs.getDouble("rate_per_night"));
        room.setMaxOccupancy(rs.getInt("max_occupancy"));
        room.setFloor(rs.getInt("floor"));
        room.setStatus(rs.getString("status"));
        return room;
    }
}
package lk.oceanview.dao;

import lk.oceanview.config.DBConnection;
import lk.oceanview.model.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * ServiceDAO - Data Access Object Pattern
 * Handles all database operations for the 'services' table.
 */
public class ServiceDAO {

    // ------------------------------------------------
    // Get all services
    // ------------------------------------------------
    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM services ORDER BY category, service_name";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ServiceDAO] getAllServices error: " + e.getMessage());
        }
        return services;
    }

    // ------------------------------------------------
    // Get only available services (for reservation form)
    // ------------------------------------------------
    public List<Service> getAvailableServices() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM services WHERE is_available = TRUE ORDER BY category, service_name";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ServiceDAO] getAvailableServices error: " + e.getMessage());
        }
        return services;
    }

    // ------------------------------------------------
    // Get service by ID
    // ------------------------------------------------
    public Service getServiceById(int serviceId) {
        String sql = "SELECT * FROM services WHERE service_id = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToService(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ServiceDAO] getServiceById error: " + e.getMessage());
        }
        return null;
    }

    // ------------------------------------------------
    // Add a new service
    // ------------------------------------------------
    public boolean addService(Service service) {
        String sql = "INSERT INTO services (service_name, description, price, category, is_available) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, service.getServiceName());
            ps.setString(2, service.getDescription());
            ps.setDouble(3, service.getPrice());
            ps.setString(4, service.getCategory());
            ps.setBoolean(5, service.isAvailable());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ServiceDAO] addService error: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Update an existing service
    // ------------------------------------------------
    public boolean updateService(Service service) {
        String sql = "UPDATE services SET service_name = ?, description = ?, price = ?, " +
                     "category = ?, is_available = ? WHERE service_id = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, service.getServiceName());
            ps.setString(2, service.getDescription());
            ps.setDouble(3, service.getPrice());
            ps.setString(4, service.getCategory());
            ps.setBoolean(5, service.isAvailable());
            ps.setInt(6, service.getServiceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ServiceDAO] updateService error: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Delete a service
    // ------------------------------------------------
    public boolean deleteService(int serviceId) {
        String sql = "DELETE FROM services WHERE service_id = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ServiceDAO] deleteService error: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Add services to a reservation
    // ------------------------------------------------
    public boolean addServiceToReservation(int reservationId, int serviceId, int quantity) {
        String sql = "INSERT INTO reservation_services (reservation_id, service_id, quantity) " +
                     "VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setInt(2, serviceId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ServiceDAO] addServiceToReservation error: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------
    // Get services for a specific reservation
    // ------------------------------------------------
    public List<Service> getServicesByReservationId(int reservationId) {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.*, rs.quantity FROM services s " +
                     "JOIN reservation_services rs ON s.service_id = rs.service_id " +
                     "WHERE rs.reservation_id = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ServiceDAO] getServicesByReservationId error: " + e.getMessage());
        }
        return services;
    }

    // ------------------------------------------------
    // Map ResultSet to Service object
    // ------------------------------------------------
    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setServiceId(rs.getInt("service_id"));
        service.setServiceName(rs.getString("service_name"));
        service.setDescription(rs.getString("description"));
        service.setPrice(rs.getDouble("price"));
        service.setCategory(rs.getString("category"));
        service.setAvailable(rs.getBoolean("is_available"));
        return service;
    }
}
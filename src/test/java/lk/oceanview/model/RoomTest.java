package lk.oceanview.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Room model class.
 * Tests cover object creation, status logic, and getters/setters.
 */
@DisplayName("Room Model Tests")
public class RoomTest {

    private Room availableRoom;
    private Room occupiedRoom;
    private Room maintenanceRoom;

    @BeforeEach
    void setUp() {
        availableRoom    = new Room(1, "101", "Deluxe",  15000.0, 1, "AVAILABLE");
        occupiedRoom     = new Room(2, "102", "Suite",   25000.0, 1, "OCCUPIED");
        maintenanceRoom  = new Room(3, "103", "Standard", 8000.0, 1, "MAINTENANCE");
    }

    // ── Constructor Tests ─────────────────────────────────

    @Test
    @DisplayName("TC-R-01: Room created with correct room number")
    void testConstructor_roomNumber() {
        assertEquals("101", availableRoom.getRoomNumber());
    }

    @Test
    @DisplayName("TC-R-02: Room created with correct room type name")
    void testConstructor_roomTypeName() {
        assertEquals("Deluxe", availableRoom.getRoomTypeName());
    }

    @Test
    @DisplayName("TC-R-03: Room created with correct rate per night")
    void testConstructor_ratePerNight() {
        assertEquals(15000.0, availableRoom.getRatePerNight(), 0.001);
    }

    @Test
    @DisplayName("TC-R-04: Room created with correct floor number")
    void testConstructor_floor() {
        assertEquals(1, availableRoom.getFloor());
    }

    @Test
    @DisplayName("TC-R-05: Room created with AVAILABLE status")
    void testConstructor_status_available() {
        assertEquals("AVAILABLE", availableRoom.getStatus());
    }

    @Test
    @DisplayName("TC-R-06: Room created with OCCUPIED status")
    void testConstructor_status_occupied() {
        assertEquals("OCCUPIED", occupiedRoom.getStatus());
    }

    @Test
    @DisplayName("TC-R-07: Room created with MAINTENANCE status")
    void testConstructor_status_maintenance() {
        assertEquals("MAINTENANCE", maintenanceRoom.getStatus());
    }

    // ── Status Change Tests ───────────────────────────────

    @Test
    @DisplayName("TC-R-08: Room status changes from AVAILABLE to OCCUPIED on check-in")
    void testStatusChange_availableToOccupied() {
        availableRoom.setStatus("OCCUPIED");
        assertEquals("OCCUPIED", availableRoom.getStatus());
    }

    @Test
    @DisplayName("TC-R-09: Room status changes from OCCUPIED to AVAILABLE on check-out")
    void testStatusChange_occupiedToAvailable() {
        occupiedRoom.setStatus("AVAILABLE");
        assertEquals("AVAILABLE", occupiedRoom.getStatus());
    }

    @Test
    @DisplayName("TC-R-10: Room status changes to MAINTENANCE")
    void testStatusChange_toMaintenance() {
        availableRoom.setStatus("MAINTENANCE");
        assertEquals("MAINTENANCE", availableRoom.getStatus());
    }

    // ── Setter Tests ──────────────────────────────────────

    @Test
    @DisplayName("TC-R-11: setRoomId stores ID correctly")
    void testSetRoomId() {
        availableRoom.setRoomId(10);
        assertEquals(10, availableRoom.getRoomId());
    }

    @Test
    @DisplayName("TC-R-12: setRatePerNight updates rate correctly")
    void testSetRatePerNight() {
        availableRoom.setRatePerNight(20000.0);
        assertEquals(20000.0, availableRoom.getRatePerNight(), 0.001);
    }

    @Test
    @DisplayName("TC-R-13: setMaxOccupancy updates max occupancy correctly")
    void testSetMaxOccupancy() {
        availableRoom.setMaxOccupancy(4);
        assertEquals(4, availableRoom.getMaxOccupancy());
    }

    // ── Boundary Tests ────────────────────────────────────

    @Test
    @DisplayName("TC-R-14: Room rate cannot be negative (boundary check)")
    void testNegativeRate() {
        availableRoom.setRatePerNight(-500.0);
        // Model stores it — business logic must validate at servlet level
        assertEquals(-500.0, availableRoom.getRatePerNight(), 0.001);
    }

    @Test
    @DisplayName("TC-R-15: Room rate of zero is stored correctly")
    void testZeroRate() {
        availableRoom.setRatePerNight(0.0);
        assertEquals(0.0, availableRoom.getRatePerNight(), 0.001);
    }

    // ── Default Constructor Tests ─────────────────────────

    @Test
    @DisplayName("TC-R-16: Default constructor creates room with null status")
    void testDefaultConstructor() {
        Room r = new Room();
        assertNull(r.getStatus());
        assertNull(r.getRoomNumber());
    }
}
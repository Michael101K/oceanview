package lk.oceanview.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Reservation model class.
 * Tests cover night calculation logic, status transitions, and field validation.
 */
@DisplayName("Reservation Model Tests")
public class ReservationTest {

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = new Reservation(
            "OVR-2025-0001",
            1,
            1,
            LocalDate.of(2025, 6, 10),
            LocalDate.of(2025, 6, 13),
            45000.0,
            "CONFIRMED",
            1
        );
    }

    // ── Constructor Tests ─────────────────────────────────

    @Test
    @DisplayName("TC-RES-01: Reservation created with correct reservation number")
    void testConstructor_reservationNumber() {
        assertEquals("OVR-2025-0001", reservation.getReservationNumber());
    }

    @Test
    @DisplayName("TC-RES-02: Reservation created with correct guest ID")
    void testConstructor_guestId() {
        assertEquals(1, reservation.getGuestId());
    }

    @Test
    @DisplayName("TC-RES-03: Reservation created with correct room ID")
    void testConstructor_roomId() {
        assertEquals(1, reservation.getRoomId());
    }

    @Test
    @DisplayName("TC-RES-04: Reservation created with correct check-in date")
    void testConstructor_checkInDate() {
        assertEquals(LocalDate.of(2025, 6, 10), reservation.getCheckInDate());
    }

    @Test
    @DisplayName("TC-RES-05: Reservation created with correct check-out date")
    void testConstructor_checkOutDate() {
        assertEquals(LocalDate.of(2025, 6, 13), reservation.getCheckOutDate());
    }

    @Test
    @DisplayName("TC-RES-06: Reservation created with correct total amount")
    void testConstructor_totalAmount() {
        assertEquals(45000.0, reservation.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("TC-RES-07: Reservation created with CONFIRMED status")
    void testConstructor_status() {
        assertEquals("CONFIRMED", reservation.getStatus());
    }

    // ── getNumNights() Logic Tests ────────────────────────

    @Test
    @DisplayName("TC-RES-08: getNumNights returns 3 for 10-Jun to 13-Jun")
    void testGetNumNights_threeDays() {
        assertEquals(3, reservation.getNumNights());
    }

    @Test
    @DisplayName("TC-RES-09: getNumNights returns 1 for same-day check-in/out")
    void testGetNumNights_oneNight() {
        reservation.setCheckInDate(LocalDate.of(2025, 7, 1));
        reservation.setCheckOutDate(LocalDate.of(2025, 7, 2));
        assertEquals(1, reservation.getNumNights());
    }

    @Test
    @DisplayName("TC-RES-10: getNumNights returns 0 when check-in and check-out are same date")
    void testGetNumNights_sameDate() {
        reservation.setCheckInDate(LocalDate.of(2025, 7, 1));
        reservation.setCheckOutDate(LocalDate.of(2025, 7, 1));
        assertEquals(0, reservation.getNumNights());
    }

    @Test
    @DisplayName("TC-RES-11: getNumNights returns 0 when dates are null")
    void testGetNumNights_nullDates() {
        Reservation r = new Reservation();
        assertEquals(0, r.getNumNights());
    }

    @Test
    @DisplayName("TC-RES-12: getNumNights returns 30 for a full month stay")
    void testGetNumNights_longStay() {
        reservation.setCheckInDate(LocalDate.of(2025, 6, 1));
        reservation.setCheckOutDate(LocalDate.of(2025, 7, 1));
        assertEquals(30, reservation.getNumNights());
    }

    // ── Status Transition Tests ───────────────────────────

    @Test
    @DisplayName("TC-RES-13: Status changes from CONFIRMED to CHECKED_IN")
    void testStatusTransition_confirmedToCheckedIn() {
        reservation.setStatus("CHECKED_IN");
        assertEquals("CHECKED_IN", reservation.getStatus());
    }

    @Test
    @DisplayName("TC-RES-14: Status changes from CHECKED_IN to CHECKED_OUT")
    void testStatusTransition_checkedInToCheckedOut() {
        reservation.setStatus("CHECKED_IN");
        reservation.setStatus("CHECKED_OUT");
        assertEquals("CHECKED_OUT", reservation.getStatus());
    }

    @Test
    @DisplayName("TC-RES-15: Status changes from CONFIRMED to CANCELLED")
    void testStatusTransition_confirmedToCancelled() {
        reservation.setStatus("CANCELLED");
        assertEquals("CANCELLED", reservation.getStatus());
    }

    // ── Setter Tests ──────────────────────────────────────

    @Test
    @DisplayName("TC-RES-16: setReservationId stores ID correctly")
    void testSetReservationId() {
        reservation.setReservationId(100);
        assertEquals(100, reservation.getReservationId());
    }

    @Test
    @DisplayName("TC-RES-17: setGuestName stores display name correctly")
    void testSetGuestName() {
        reservation.setGuestName("Kasun Perera");
        assertEquals("Kasun Perera", reservation.getGuestName());
    }

    @Test
    @DisplayName("TC-RES-18: setRoomNumber stores display room number correctly")
    void testSetRoomNumber() {
        reservation.setRoomNumber("204");
        assertEquals("204", reservation.getRoomNumber());
    }

    // ── Default Constructor Tests ─────────────────────────

    @Test
    @DisplayName("TC-RES-19: Default constructor creates reservation with null status")
    void testDefaultConstructor_nullStatus() {
        Reservation r = new Reservation();
        assertNull(r.getStatus());
    }

    @Test
    @DisplayName("TC-RES-20: Default constructor creates reservation with zero total amount")
    void testDefaultConstructor_zeroTotal() {
        Reservation r = new Reservation();
        assertEquals(0.0, r.getTotalAmount(), 0.001);
    }
}
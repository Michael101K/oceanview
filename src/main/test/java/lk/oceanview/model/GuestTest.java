package lk.oceanview.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Guest model class.
 * Tests cover object creation, getters/setters, and boundary conditions.
 */
@DisplayName("Guest Model Tests")
public class GuestTest {

    private Guest guest;

    @BeforeEach
    void setUp() {
        guest = new Guest("Kasun Perera", "123 Galle Road, Colombo", "0771234567", "kasun@email.com", "123456789V");
    }

    // ── Constructor Tests ──────────────────────────────────

    @Test
    @DisplayName("TC-G-01: Guest created with correct full name")
    void testConstructor_fullName() {
        assertEquals("Kasun Perera", guest.getFullName());
    }

    @Test
    @DisplayName("TC-G-02: Guest created with correct address")
    void testConstructor_address() {
        assertEquals("123 Galle Road, Colombo", guest.getAddress());
    }

    @Test
    @DisplayName("TC-G-03: Guest created with correct contact number")
    void testConstructor_contactNumber() {
        assertEquals("0771234567", guest.getContactNumber());
    }

    @Test
    @DisplayName("TC-G-04: Guest created with correct email")
    void testConstructor_email() {
        assertEquals("kasun@email.com", guest.getEmail());
    }

    @Test
    @DisplayName("TC-G-05: Guest created with correct NIC number")
    void testConstructor_nicNumber() {
        assertEquals("123456789V", guest.getNicNumber());
    }

    // ── Setter Tests ──────────────────────────────────────

    @Test
    @DisplayName("TC-G-06: setGuestId stores and retrieves ID correctly")
    void testSetGuestId() {
        guest.setGuestId(42);
        assertEquals(42, guest.getGuestId());
    }

    @Test
    @DisplayName("TC-G-07: setFullName updates the name correctly")
    void testSetFullName() {
        guest.setFullName("Nimal Silva");
        assertEquals("Nimal Silva", guest.getFullName());
    }

    @Test
    @DisplayName("TC-G-08: setEmail updates email correctly")
    void testSetEmail() {
        guest.setEmail("nimal@gmail.com");
        assertEquals("nimal@gmail.com", guest.getEmail());
    }

    // ── Default Constructor Tests ─────────────────────────

    @Test
    @DisplayName("TC-G-09: Default constructor creates guest with null fields")
    void testDefaultConstructor() {
        Guest emptyGuest = new Guest();
        assertNull(emptyGuest.getFullName());
        assertNull(emptyGuest.getNicNumber());
    }

    @Test
    @DisplayName("TC-G-10: Default constructor creates guest with guestId 0")
    void testDefaultConstructor_idIsZero() {
        Guest emptyGuest = new Guest();
        assertEquals(0, emptyGuest.getGuestId());
    }

    // ── Boundary Tests ────────────────────────────────────

    @Test
    @DisplayName("TC-G-11: Guest accepts empty string email (optional field)")
    void testEmptyEmail() {
        guest.setEmail("");
        assertEquals("", guest.getEmail());
    }

    @Test
    @DisplayName("TC-G-12: Guest full name accepts long strings")
    void testLongFullName() {
        String longName = "Abcdefghijklmnopqrstuvwxyz Abcdefghijklmnopqrstuvwxyz";
        guest.setFullName(longName);
        assertEquals(longName, guest.getFullName());
    }
}
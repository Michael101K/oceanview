package lk.oceanview.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the User model class.
 * Tests cover object creation, role-based logic, getters/setters, and boundary conditions.
 */
@DisplayName("User Model Tests")
public class UserTest {

    private User adminUser;
    private User receptionistUser;

    @BeforeEach
    void setUp() {
        adminUser        = new User(1, "admin",    "Admin User",  "ADMIN",         "admin@oceanview.lk",  true);
        receptionistUser = new User(2, "john",     "John Silva",  "RECEPTIONIST",  "john@oceanview.lk",   true);
    }

    // ── Constructor Tests ─────────────────────────────────

    @Test
    @DisplayName("TC-U-01: Admin user created with correct username")
    void testConstructor_username() {
        assertEquals("admin", adminUser.getUsername());
    }

    @Test
    @DisplayName("TC-U-02: Admin user created with correct full name")
    void testConstructor_fullName() {
        assertEquals("Admin User", adminUser.getFullName());
    }

    @Test
    @DisplayName("TC-U-03: Admin user created with correct role")
    void testConstructor_role_admin() {
        assertEquals("ADMIN", adminUser.getRole());
    }

    @Test
    @DisplayName("TC-U-04: Receptionist user created with correct role")
    void testConstructor_role_receptionist() {
        assertEquals("RECEPTIONIST", receptionistUser.getRole());
    }

    @Test
    @DisplayName("TC-U-05: User created with isActive = true")
    void testConstructor_isActive() {
        assertTrue(adminUser.isActive());
    }

    // ── isAdmin() Logic Tests ─────────────────────────────

    @Test
    @DisplayName("TC-U-06: isAdmin() returns true for ADMIN role")
    void testIsAdmin_returnsTrue_forAdmin() {
        assertTrue(adminUser.isAdmin());
    }

    @Test
    @DisplayName("TC-U-07: isAdmin() returns false for RECEPTIONIST role")
    void testIsAdmin_returnsFalse_forReceptionist() {
        assertFalse(receptionistUser.isAdmin());
    }

    @Test
    @DisplayName("TC-U-08: isAdmin() returns false when role is null")
    void testIsAdmin_returnsFalse_whenRoleNull() {
        User u = new User();
        assertFalse(u.isAdmin());
    }

    @Test
    @DisplayName("TC-U-09: isAdmin() returns false for unknown role")
    void testIsAdmin_returnsFalse_forUnknownRole() {
        adminUser.setRole("MANAGER");
        assertFalse(adminUser.isAdmin());
    }

    // ── isActive Tests ────────────────────────────────────

    @Test
    @DisplayName("TC-U-10: setActive false deactivates user")
    void testSetActive_false() {
        adminUser.setActive(false);
        assertFalse(adminUser.isActive());
    }

    @Test
    @DisplayName("TC-U-11: setActive true activates user")
    void testSetActive_true() {
        receptionistUser.setActive(false);
        receptionistUser.setActive(true);
        assertTrue(receptionistUser.isActive());
    }

    // ── Setter Tests ──────────────────────────────────────

    @Test
    @DisplayName("TC-U-12: setUserId stores and retrieves ID correctly")
    void testSetUserId() {
        adminUser.setUserId(99);
        assertEquals(99, adminUser.getUserId());
    }

    @Test
    @DisplayName("TC-U-13: setEmail updates email correctly")
    void testSetEmail() {
        adminUser.setEmail("new@oceanview.lk");
        assertEquals("new@oceanview.lk", adminUser.getEmail());
    }

    @Test
    @DisplayName("TC-U-14: setPassword stores password")
    void testSetPassword() {
        adminUser.setPassword("hashed_value");
        assertEquals("hashed_value", adminUser.getPassword());
    }

    // ── Default Constructor Tests ─────────────────────────

    @Test
    @DisplayName("TC-U-15: Default constructor creates user with null username")
    void testDefaultConstructor() {
        User u = new User();
        assertNull(u.getUsername());
    }

    @Test
    @DisplayName("TC-U-16: Default constructor creates user with userId 0")
    void testDefaultConstructor_idIsZero() {
        User u = new User();
        assertEquals(0, u.getUserId());
    }
}
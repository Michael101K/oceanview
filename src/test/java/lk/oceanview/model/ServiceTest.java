package lk.oceanview.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Service model class.
 * Tests cover object creation, availability toggling, and field storage.
 */
@DisplayName("Service Model Tests")
public class ServiceTest {

    private Service service;

    @BeforeEach
    void setUp() {
        service = new Service();
        service.setServiceId(1);
        service.setServiceName("Spa Treatment");
        service.setDescription("Full body spa and massage");
        service.setPrice(5000.0);
        service.setCategory("Wellness");
        service.setAvailable(true);
    }

    // ── Field Storage Tests ───────────────────────────────

    @Test
    @DisplayName("TC-SVC-01: Service stores name correctly")
    void testGetServiceName() {
        assertEquals("Spa Treatment", service.getServiceName());
    }

    @Test
    @DisplayName("TC-SVC-02: Service stores price correctly")
    void testGetPrice() {
        assertEquals(5000.0, service.getPrice(), 0.001);
    }

    @Test
    @DisplayName("TC-SVC-03: Service stores category correctly")
    void testGetCategory() {
        assertEquals("Wellness", service.getCategory());
    }

    @Test
    @DisplayName("TC-SVC-04: Service stores description correctly")
    void testGetDescription() {
        assertEquals("Full body spa and massage", service.getDescription());
    }

    @Test
    @DisplayName("TC-SVC-05: Service stores ID correctly")
    void testGetServiceId() {
        assertEquals(1, service.getServiceId());
    }

    // ── Availability Tests ────────────────────────────────

    @Test
    @DisplayName("TC-SVC-06: Service is available by default after setup")
    void testIsAvailable_true() {
        assertTrue(service.isAvailable());
    }

    @Test
    @DisplayName("TC-SVC-07: setAvailable(false) disables service")
    void testSetAvailable_false() {
        service.setAvailable(false);
        assertFalse(service.isAvailable());
    }

    @Test
    @DisplayName("TC-SVC-08: setAvailable(true) re-enables service")
    void testSetAvailable_reEnable() {
        service.setAvailable(false);
        service.setAvailable(true);
        assertTrue(service.isAvailable());
    }

    // ── Setter Tests ──────────────────────────────────────

    @Test
    @DisplayName("TC-SVC-09: setPrice updates price correctly")
    void testSetPrice() {
        service.setPrice(7500.0);
        assertEquals(7500.0, service.getPrice(), 0.001);
    }

    @Test
    @DisplayName("TC-SVC-10: setServiceName updates name correctly")
    void testSetServiceName() {
        service.setServiceName("Airport Transfer");
        assertEquals("Airport Transfer", service.getServiceName());
    }

    @Test
    @DisplayName("TC-SVC-11: setCategory updates category correctly")
    void testSetCategory() {
        service.setCategory("Transport");
        assertEquals("Transport", service.getCategory());
    }

    // ── Default Constructor Tests ─────────────────────────

    @Test
    @DisplayName("TC-SVC-12: Default constructor creates service with null name")
    void testDefaultConstructor_nullName() {
        Service s = new Service();
        assertNull(s.getServiceName());
    }

    @Test
    @DisplayName("TC-SVC-13: Default constructor creates service with zero price")
    void testDefaultConstructor_zeroPrice() {
        Service s = new Service();
        assertEquals(0.0, s.getPrice(), 0.001);
    }

    // ── Boundary Tests ────────────────────────────────────

    @Test
    @DisplayName("TC-SVC-14: Service price of zero is stored correctly")
    void testZeroPrice() {
        service.setPrice(0.0);
        assertEquals(0.0, service.getPrice(), 0.001);
    }

    @Test
    @DisplayName("TC-SVC-15: Service accepts empty string category")
    void testEmptyCategory() {
        service.setCategory("");
        assertEquals("", service.getCategory());
    }
}
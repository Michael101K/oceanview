package lk.oceanview.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the RoomType model class.
 */
@DisplayName("RoomType Model Tests")
public class RoomTypeTest {

    private RoomType deluxeType;

    @BeforeEach
    void setUp() {
        deluxeType = new RoomType();
        deluxeType.setRoomTypeId(1);
        deluxeType.setTypeName("Deluxe");
        deluxeType.setDescription("Comfortable deluxe room with ocean view");
        deluxeType.setRatePerNight(15000.0);
        deluxeType.setMaxOccupancy(2);
    }

    @Test
    @DisplayName("TC-RT-01: RoomType stores type name correctly")
    void testGetTypeName() {
        assertEquals("Deluxe", deluxeType.getTypeName());
    }

    @Test
    @DisplayName("TC-RT-02: RoomType stores rate per night correctly")
    void testGetRatePerNight() {
        assertEquals(15000.0, deluxeType.getRatePerNight(), 0.001);
    }

    @Test
    @DisplayName("TC-RT-03: RoomType stores max occupancy correctly")
    void testGetMaxOccupancy() {
        assertEquals(2, deluxeType.getMaxOccupancy());
    }

    @Test
    @DisplayName("TC-RT-04: RoomType stores description correctly")
    void testGetDescription() {
        assertEquals("Comfortable deluxe room with ocean view", deluxeType.getDescription());
    }

    @Test
    @DisplayName("TC-RT-05: RoomType setTypeName updates correctly")
    void testSetTypeName() {
        deluxeType.setTypeName("Suite");
        assertEquals("Suite", deluxeType.getTypeName());
    }

    @Test
    @DisplayName("TC-RT-06: RoomType setRatePerNight updates correctly")
    void testSetRatePerNight() {
        deluxeType.setRatePerNight(25000.0);
        assertEquals(25000.0, deluxeType.getRatePerNight(), 0.001);
    }

    @Test
    @DisplayName("TC-RT-07: RoomType getRoomTypeId returns set ID")
    void testGetRoomTypeId() {
        assertEquals(1, deluxeType.getRoomTypeId());
    }

    @Test
    @DisplayName("TC-RT-08: Default RoomType has zero rate")
    void testDefaultRate() {
        RoomType rt = new RoomType();
        assertEquals(0.0, rt.getRatePerNight(), 0.001);
    }
}
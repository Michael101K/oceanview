package lk.oceanview;

import lk.oceanview.model.BillTest;
import lk.oceanview.model.GuestTest;
import lk.oceanview.model.ReservationTest;
import lk.oceanview.model.RoomTest;
import lk.oceanview.model.RoomTypeTest;
import lk.oceanview.model.UserTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite that runs all unit tests for the Ocean View Resort system.
 * Used for full test automation via: mvn test
 */
@Suite
@SelectClasses({
    UserTest.class,
    GuestTest.class,
    RoomTest.class,
    RoomTypeTest.class,
    ReservationTest.class,
    BillTest.class
})
public class AllTests {
    // This class acts as a test runner entry point.
    // Run with: mvn test
    // Or right-click → Run in IntelliJ IDEA / Eclipse
}
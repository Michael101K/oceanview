package lk.oceanview.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Bill model class.
 * This class is the most critical to test as it contains financial calculation logic.
 * Tests follow TDD: these tests were written before/alongside the Bill.calculateBill() method
 * to ensure correct VAT, discount, and total amount calculations.
 */
@DisplayName("Bill Model Tests")
public class BillTest {

    private static final double DELTA = 0.001;

    // ── calculateBill() Tests ─────────────────────────────

    @Test
    @DisplayName("TC-B-01: calculateBill computes room charges correctly (3 nights x LKR 10,000)")
    void testCalculateBill_roomCharges() {
        Bill bill = Bill.calculateBill(1, 3, 10000.0, 0.0, "CASH");
        assertEquals(30000.0, bill.getRoomCharges(), DELTA);
    }

    @Test
    @DisplayName("TC-B-02: calculateBill computes 10% VAT on room charges correctly")
    void testCalculateBill_vatAmount_noDiscount() {
        // 3 nights x LKR 10,000 = LKR 30,000 room charges
        // VAT = 30,000 x 10% = LKR 3,000
        Bill bill = Bill.calculateBill(1, 3, 10000.0, 0.0, "CASH");
        assertEquals(3000.0, bill.getTaxAmount(), DELTA);
    }

    @Test
    @DisplayName("TC-B-03: calculateBill total = room charges + VAT when no discount")
    void testCalculateBill_totalAmount_noDiscount() {
        // 3 nights x LKR 10,000 = LKR 30,000
        // VAT = LKR 3,000
        // Total = LKR 33,000
        Bill bill = Bill.calculateBill(1, 3, 10000.0, 0.0, "CASH");
        assertEquals(33000.0, bill.getTotalAmount(), DELTA);
    }

    @Test
    @DisplayName("TC-B-04: calculateBill applies discount before VAT calculation")
    void testCalculateBill_vatAfterDiscount() {
        // 2 nights x LKR 15,000 = LKR 30,000 room charges
        // Discount = LKR 3,000
        // VAT = (30,000 - 3,000) x 10% = LKR 2,700
        Bill bill = Bill.calculateBill(1, 2, 15000.0, 3000.0, "CARD");
        assertEquals(2700.0, bill.getTaxAmount(), DELTA);
    }

    @Test
    @DisplayName("TC-B-05: calculateBill total is correct with discount applied")
    void testCalculateBill_totalAmount_withDiscount() {
        // 2 nights x LKR 15,000 = LKR 30,000
        // Discount = LKR 3,000
        // VAT = LKR 2,700
        // Total = 30,000 - 3,000 + 2,700 = LKR 29,700
        Bill bill = Bill.calculateBill(1, 2, 15000.0, 3000.0, "CARD");
        assertEquals(29700.0, bill.getTotalAmount(), DELTA);
    }

    @Test
    @DisplayName("TC-B-06: calculateBill with 1 night stay calculates correctly")
    void testCalculateBill_oneNight() {
        // 1 night x LKR 8,000 = LKR 8,000
        // VAT = LKR 800
        // Total = LKR 8,800
        Bill bill = Bill.calculateBill(2, 1, 8000.0, 0.0, "CASH");
        assertEquals(8800.0, bill.getTotalAmount(), DELTA);
    }

    @Test
    @DisplayName("TC-B-07: calculateBill sets reservationId correctly")
    void testCalculateBill_reservationId() {
        Bill bill = Bill.calculateBill(5, 2, 10000.0, 0.0, "CASH");
        assertEquals(5, bill.getReservationId());
    }

    @Test
    @DisplayName("TC-B-08: calculateBill sets payment method correctly")
    void testCalculateBill_paymentMethod() {
        Bill bill = Bill.calculateBill(1, 2, 10000.0, 0.0, "ONLINE");
        assertEquals("ONLINE", bill.getPaymentMethod());
    }

    @Test
    @DisplayName("TC-B-09: TAX_RATE constant is 10%")
    void testTaxRateConstant() {
        assertEquals(0.10, Bill.TAX_RATE, DELTA);
    }

    @Test
    @DisplayName("TC-B-10: calculateBill with zero discount equals full room charge plus VAT")
    void testCalculateBill_zeroDiscount() {
        Bill bill = Bill.calculateBill(1, 5, 12000.0, 0.0, "CASH");
        double expectedRoomCharges = 5 * 12000.0;   // 60,000
        double expectedVat         = 60000.0 * 0.10; // 6,000
        double expectedTotal       = 60000.0 + 6000.0; // 66,000
        assertEquals(expectedRoomCharges, bill.getRoomCharges(), DELTA);
        assertEquals(expectedVat,         bill.getTaxAmount(),   DELTA);
        assertEquals(expectedTotal,       bill.getTotalAmount(), DELTA);
    }

    @Test
    @DisplayName("TC-B-11: calculateBill with 100% discount (full waiver)")
    void testCalculateBill_fullDiscount() {
        // Room charges = LKR 10,000. Full waiver (discount = 10,000).
        // VAT = (10,000 - 10,000) * 10% = 0
        // Total = 10,000 - 10,000 + 0 = 0
        Bill bill = Bill.calculateBill(1, 1, 10000.0, 10000.0, "CASH");
        assertEquals(0.0, bill.getTaxAmount(),   DELTA);
        assertEquals(0.0, bill.getTotalAmount(), DELTA);
    }

    // ── Constructor Tests ─────────────────────────────────

    @Test
    @DisplayName("TC-B-12: Bill constructor sets payment status to PENDING by default")
    void testConstructor_defaultPaymentStatus() {
        Bill bill = new Bill(1, 20000.0, 0.0, "CASH");
        assertEquals("PENDING", bill.getPaymentStatus());
    }

    @Test
    @DisplayName("TC-B-13: setPaymentStatus updates to PAID correctly")
    void testSetPaymentStatus_toPaid() {
        Bill bill = new Bill(1, 20000.0, 0.0, "CASH");
        bill.setPaymentStatus("PAID");
        assertEquals("PAID", bill.getPaymentStatus());
    }

    // ── Setter Tests ──────────────────────────────────────

    @Test
    @DisplayName("TC-B-14: setBillId stores ID correctly")
    void testSetBillId() {
        Bill bill = new Bill();
        bill.setBillId(7);
        assertEquals(7, bill.getBillId());
    }

    @Test
    @DisplayName("TC-B-15: setDiscountAmount stores discount correctly")
    void testSetDiscountAmount() {
        Bill bill = new Bill();
        bill.setDiscountAmount(500.0);
        assertEquals(500.0, bill.getDiscountAmount(), DELTA);
    }
}
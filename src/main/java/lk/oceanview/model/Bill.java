package lk.oceanview.model;

import java.time.LocalDateTime;

/**
 * Bill Model
 * Represents a generated bill for a reservation.
 * Maps to the 'bills' table in the database.
 */
public class Bill {

    private int billId;
    private int reservationId;
    private double roomCharges;
    private double taxAmount;           // 10% VAT
    private double discountAmount;
    private double totalAmount;
    private String paymentStatus;       // PENDING, PAID, PARTIALLY_PAID
    private String paymentMethod;       // CASH, CARD, ONLINE
    private LocalDateTime generatedAt;

    // Extra fields for display
    private String reservationNumber;
    private String guestName;
    private long numNights;
    private double ratePerNight;

    // Tax rate constant
    public static final double TAX_RATE = 0.10; // 10% VAT

    // ------------------------------------------------
    // Constructors
    // ------------------------------------------------
    public Bill() {}

    public Bill(int reservationId, double roomCharges, double discountAmount, String paymentMethod) {
        this.reservationId   = reservationId;
        this.roomCharges     = roomCharges;
        this.discountAmount  = discountAmount;
        this.taxAmount       = (roomCharges - discountAmount) * TAX_RATE;
        this.totalAmount     = (roomCharges - discountAmount) + this.taxAmount;
        this.paymentMethod   = paymentMethod;
        this.paymentStatus   = "PENDING";
    }

    // ------------------------------------------------
    // Calculated method - compute bill from nights & rate
    // ------------------------------------------------
    public static Bill calculateBill(int reservationId, long numNights,
                                     double ratePerNight, double discountAmount,
                                     String paymentMethod) {
        double roomCharges = numNights * ratePerNight;
        return new Bill(reservationId, roomCharges, discountAmount, paymentMethod);
    }

    // ------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------
    public int getBillId()                               { return billId; }
    public void setBillId(int billId)                    { this.billId = billId; }

    public int getReservationId()                        { return reservationId; }
    public void setReservationId(int reservationId)      { this.reservationId = reservationId; }

    public double getRoomCharges()                       { return roomCharges; }
    public void setRoomCharges(double roomCharges)       { this.roomCharges = roomCharges; }

    public double getTaxAmount()                         { return taxAmount; }
    public void setTaxAmount(double taxAmount)           { this.taxAmount = taxAmount; }

    public double getDiscountAmount()                            { return discountAmount; }
    public void setDiscountAmount(double discountAmount)         { this.discountAmount = discountAmount; }

    public double getTotalAmount()                       { return totalAmount; }
    public void setTotalAmount(double totalAmount)       { this.totalAmount = totalAmount; }

    public String getPaymentStatus()                             { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus)           { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod()                             { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod)           { this.paymentMethod = paymentMethod; }

    public LocalDateTime getGeneratedAt()                        { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt)        { this.generatedAt = generatedAt; }

    public String getReservationNumber()                                 { return reservationNumber; }
    public void setReservationNumber(String reservationNumber)           { this.reservationNumber = reservationNumber; }

    public String getGuestName()                                 { return guestName; }
    public void setGuestName(String guestName)                   { this.guestName = guestName; }

    public long getNumNights()                                   { return numNights; }
    public void setNumNights(long numNights)                     { this.numNights = numNights; }

    public double getRatePerNight()                              { return ratePerNight; }
    public void setRatePerNight(double ratePerNight)             { this.ratePerNight = ratePerNight; }

    @Override
    public String toString() {
        return "Bill{reservationNumber='" + reservationNumber + "', roomCharges=" + roomCharges +
               ", tax=" + taxAmount + ", discount=" + discountAmount +
               ", total=" + totalAmount + ", status='" + paymentStatus + "'}";
    }
}
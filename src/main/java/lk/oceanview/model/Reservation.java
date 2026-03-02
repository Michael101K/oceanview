package lk.oceanview.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Reservation Model
 * Represents a room booking made by a guest.
 * Maps to the 'reservations' table in the database.
 */
public class Reservation {

    private int reservationId;
    private String reservationNumber;   // e.g. OVR-2025-0001
    private int guestId;
    private int roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalAmount;
    private String status;              // CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
    private String specialRequests;
    private int createdBy;              // user_id of staff

    // Extra fields for display (joined from other tables)
    private String guestName;
    private String roomNumber;
    private String roomTypeName;
    private double ratePerNight;

    // ------------------------------------------------
    // Constructors
    // ------------------------------------------------
    public Reservation() {}

    public Reservation(String reservationNumber, int guestId, int roomId,
                       LocalDate checkInDate, LocalDate checkOutDate,
                       double totalAmount, String status, int createdBy) {
        this.reservationNumber = reservationNumber;
        this.guestId           = guestId;
        this.roomId            = roomId;
        this.checkInDate       = checkInDate;
        this.checkOutDate      = checkOutDate;
        this.totalAmount       = totalAmount;
        this.status            = status;
        this.createdBy         = createdBy;
    }

    // ------------------------------------------------
    // Calculated field - number of nights
    // ------------------------------------------------
    public long getNumNights() {
        if (checkInDate != null && checkOutDate != null) {
            return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        }
        return 0;
    }

    // ------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------
    public int getReservationId()                            { return reservationId; }
    public void setReservationId(int reservationId)          { this.reservationId = reservationId; }

    public String getReservationNumber()                             { return reservationNumber; }
    public void setReservationNumber(String reservationNumber)       { this.reservationNumber = reservationNumber; }

    public int getGuestId()                                  { return guestId; }
    public void setGuestId(int guestId)                      { this.guestId = guestId; }

    public int getRoomId()                                   { return roomId; }
    public void setRoomId(int roomId)                        { this.roomId = roomId; }

    public LocalDate getCheckInDate()                                { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate)                { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate()                               { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate)              { this.checkOutDate = checkOutDate; }

    public double getTotalAmount()                           { return totalAmount; }
    public void setTotalAmount(double totalAmount)           { this.totalAmount = totalAmount; }

    public String getStatus()                                { return status; }
    public void setStatus(String status)                     { this.status = status; }

    public String getSpecialRequests()                               { return specialRequests; }
    public void setSpecialRequests(String specialRequests)           { this.specialRequests = specialRequests; }

    public int getCreatedBy()                                { return createdBy; }
    public void setCreatedBy(int createdBy)                  { this.createdBy = createdBy; }

    public String getGuestName()                             { return guestName; }
    public void setGuestName(String guestName)               { this.guestName = guestName; }

    public String getRoomNumber()                            { return roomNumber; }
    public void setRoomNumber(String roomNumber)             { this.roomNumber = roomNumber; }

    public String getRoomTypeName()                          { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName)         { this.roomTypeName = roomTypeName; }

    public double getRatePerNight()                          { return ratePerNight; }
    public void setRatePerNight(double ratePerNight)         { this.ratePerNight = ratePerNight; }

    @Override
    public String toString() {
        return "Reservation{number='" + reservationNumber + "', guest='" + guestName +
               "', room='" + roomNumber + "', checkIn=" + checkInDate +
               ", checkOut=" + checkOutDate + ", status='" + status + "'}";
    }
}
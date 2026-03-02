package lk.oceanview.model;

/**
 * Room Model
 * Represents a physical hotel room.
 * Maps to the 'rooms' and 'room_types' tables in the database.
 */
public class Room {

    private int roomId;
    private String roomNumber;
    private int roomTypeId;
    private String roomTypeName;
    private double ratePerNight;
    private int maxOccupancy;
    private int floor;
    private String status;      // AVAILABLE, OCCUPIED, MAINTENANCE

    // ------------------------------------------------
    // Constructors
    // ------------------------------------------------
    public Room() {}

    public Room(int roomId, String roomNumber, String roomTypeName, double ratePerNight, int floor, String status) {
        this.roomId       = roomId;
        this.roomNumber   = roomNumber;
        this.roomTypeName = roomTypeName;
        this.ratePerNight = ratePerNight;
        this.floor        = floor;
        this.status       = status;
    }

    // ------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------
    public int getRoomId()                       { return roomId; }
    public void setRoomId(int roomId)            { this.roomId = roomId; }

    public String getRoomNumber()                        { return roomNumber; }
    public void setRoomNumber(String roomNumber)         { this.roomNumber = roomNumber; }

    public int getRoomTypeId()                           { return roomTypeId; }
    public void setRoomTypeId(int roomTypeId)            { this.roomTypeId = roomTypeId; }

    public String getRoomTypeName()                      { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName)     { this.roomTypeName = roomTypeName; }

    public double getRatePerNight()                      { return ratePerNight; }
    public void setRatePerNight(double ratePerNight)     { this.ratePerNight = ratePerNight; }

    public int getMaxOccupancy()                         { return maxOccupancy; }
    public void setMaxOccupancy(int maxOccupancy)        { this.maxOccupancy = maxOccupancy; }

    public int getFloor()                        { return floor; }
    public void setFloor(int floor)              { this.floor = floor; }

    public String getStatus()                    { return status; }
    public void setStatus(String status)         { this.status = status; }

    // ------------------------------------------------
    // Helper
    // ------------------------------------------------
    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return "Room{roomNumber='" + roomNumber + "', type='" + roomTypeName + "', rate=" + ratePerNight + ", status='" + status + "'}";
    }
}
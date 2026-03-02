package lk.oceanview.model;

/**
 * Guest Model
 * Represents a hotel guest.
 * Maps to the 'guests' table in the database.
 */
public class Guest {

    private int guestId;
    private String fullName;
    private String address;
    private String contactNumber;
    private String email;
    private String nicNumber;

    // ------------------------------------------------
    // Constructors
    // ------------------------------------------------
    public Guest() {}

    public Guest(String fullName, String address, String contactNumber, String email, String nicNumber) {
        this.fullName      = fullName;
        this.address       = address;
        this.contactNumber = contactNumber;
        this.email         = email;
        this.nicNumber     = nicNumber;
    }

    // ------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------
    public int getGuestId()                      { return guestId; }
    public void setGuestId(int guestId)          { this.guestId = guestId; }

    public String getFullName()                  { return fullName; }
    public void setFullName(String fullName)     { this.fullName = fullName; }

    public String getAddress()                   { return address; }
    public void setAddress(String address)       { this.address = address; }

    public String getContactNumber()                         { return contactNumber; }
    public void setContactNumber(String contactNumber)       { this.contactNumber = contactNumber; }

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }

    public String getNicNumber()                 { return nicNumber; }
    public void setNicNumber(String nicNumber)   { this.nicNumber = nicNumber; }

    @Override
    public String toString() {
        return "Guest{guestId=" + guestId + ", fullName='" + fullName + "', contact='" + contactNumber + "'}";
    }
}
package lk.oceanview.model;

/**
 * User Model
 * Represents a staff member who can log into the system.
 * Maps to the 'users' table in the database.
 */
public class User {

    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String role;        // ADMIN or RECEPTIONIST
    private String email;
    private boolean isActive;

    // ------------------------------------------------
    // Constructors
    // ------------------------------------------------
    public User() {}

    public User(int userId, String username, String fullName, String role, String email, boolean isActive) {
        this.userId   = userId;
        this.username = username;
        this.fullName = fullName;
        this.role     = role;
        this.email    = email;
        this.isActive = isActive;
    }

    // ------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------
    public int getUserId()               { return userId; }
    public void setUserId(int userId)    { this.userId = userId; }

    public String getUsername()                  { return username; }
    public void setUsername(String username)     { this.username = username; }

    public String getPassword()                  { return password; }
    public void setPassword(String password)     { this.password = password; }

    public String getFullName()                  { return fullName; }
    public void setFullName(String fullName)     { this.fullName = fullName; }

    public String getRole()                      { return role; }
    public void setRole(String role)             { this.role = role; }

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }

    public boolean isActive()                    { return isActive; }
    public void setActive(boolean active)        { isActive = active; }

    // ------------------------------------------------
    // Helper
    // ------------------------------------------------
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", username='" + username + "', role='" + role + "'}";
    }
}
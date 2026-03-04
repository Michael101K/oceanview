package lk.oceanview.model;

/**
 * Service Model
 * Represents a service offered by Ocean View Resort.
 * Maps to the 'services' table in the database.
 */
public class Service {

    private int serviceId;
    private String serviceName;
    private String description;
    private double price;
    private String category;
    private boolean isAvailable;

    // ------------------------------------------------
    // Constructors
    // ------------------------------------------------
    public Service() {}

    public Service(String serviceName, String description, double price,
                   String category, boolean isAvailable) {
        this.serviceName = serviceName;
        this.description = description;
        this.price       = price;
        this.category    = category;
        this.isAvailable = isAvailable;
    }

    // ------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------
    public int getServiceId()                        { return serviceId; }
    public void setServiceId(int serviceId)          { this.serviceId = serviceId; }

    public String getServiceName()                           { return serviceName; }
    public void setServiceName(String serviceName)           { this.serviceName = serviceName; }

    public String getDescription()                           { return description; }
    public void setDescription(String description)           { this.description = description; }

    public double getPrice()                         { return price; }
    public void setPrice(double price)               { this.price = price; }

    public String getCategory()                      { return category; }
    public void setCategory(String category)         { this.category = category; }

    public boolean isAvailable()                     { return isAvailable; }
    public void setAvailable(boolean available)      { isAvailable = available; }

    @Override
    public String toString() {
        return "Service{serviceId=" + serviceId + ", serviceName='" + serviceName +
               "', price=" + price + ", category='" + category + "'}";
    }
}
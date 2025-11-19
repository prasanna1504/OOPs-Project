package com.smartpark.model;

/*
 * Represents a reservation or completed parking session.
 * Stores user details, slot assignment, status, and timestamps for billing.
 */
public class Booking {

    private Integer bookingId;        // Unique identifier for the booking
    private String username;          // Name of the user who made the booking
    private int slotId;               // ID of the assigned parking slot
    private String status;            // PENDING, ACTIVE, COMPLETED, CANCELLED
    private Double amount;            // Final fee charged (null until exit)

    // TIMESTAMPS FOR TRACKING AND BILLING
    private long creationTime;        // When the booking was made (for auto-expiration)
    private long entryTime;           // When the vehicle physically entered
    private long exitTime;            // When the vehicle physically exited

    /*
     * Constructor 1: Basic booking creation.
     * Captures the creation timestamp immediately.
     */
    public Booking(String username, int slotId) {
        this.bookingId = null;
        this.username = username;
        this.slotId = slotId;
        this.status = Constants.STATUS_PENDING;
        this.amount = null;
        
        // Initialize timestamps
        this.creationTime = System.currentTimeMillis();
        this.entryTime = 0;
        this.exitTime = 0;
    }

    /*
     * Constructor 2: Overloaded constructor with specific booking ID.
     * Useful for loading data or testing.
     */
    public Booking(Integer bookingId, String username, int slotId) {
        this.bookingId = bookingId;
        this.username = username;
        this.slotId = slotId;
        this.status = Constants.STATUS_PENDING;
        this.amount = null;
        
        this.creationTime = System.currentTimeMillis();
        this.entryTime = 0;
        this.exitTime = 0;
    }

    // GETTERS AND SETTERS

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    // TIMESTAMP ACCESSORS

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime;
    }

    public long getExitTime() {
        return exitTime;
    }

    public void setExitTime(long exitTime) {
        this.exitTime = exitTime;
    }

    @Override
    public String toString() {
        String idStr = (bookingId == null) ? "unassigned" : String.valueOf(bookingId);
        String amtStr = (amount == null) ? "pending" : String.format("$%.2f", amount);
        String userStr = (username == null) ? "unknown" : username;

        return "Booking[id=" + idStr +
               ", user=" + userStr +
               ", slot=" + slotId +
               ", status=" + status +
               ", amount=" + amtStr + "]";
    }
}
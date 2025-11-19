package com.smartpark.model;

/*
 * Basic user class
 * - Uses arrays for storing booking IDs (no Collections)
 * - Overloaded constructors
 * - Wrapper Integer used for userId
 */
public class User {

    private Integer userId;           
    private String username;
    private String password;
    private String role;

    // Simple array to store booking IDs
    private int[] bookingIds;
    private int bookingCount;         

    // Overloaded constructors

    // Constructor 1: username only
    public User(String username) {
        this.username = username;
        this.password = "";
        this.role = Constants.ROLE_USER;
        this.bookingIds = new int[10];    
        this.bookingCount = 0;
        this.userId = null;               
    }

    // Constructor 2: username + password
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.role = Constants.ROLE_USER;
        this.bookingIds = new int[10];
        this.bookingCount = 0;
        this.userId = null;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Null-safe password check.
     * Returns true if both are null or if strings match.
     */
    public boolean checkPassword(String attempt) {
        if (this.password == null) {
            return attempt == null;
        }
        return this.password.equals(attempt);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Add a booking id to the user's list.
     * Automatically resizes the backing array when full.
     */
    public void addBookingId(int bookingId) {
        if (bookingIds == null) {
            bookingIds = new int[10];
            bookingCount = 0;
        }
        if (bookingCount == bookingIds.length) {
            int newSize = bookingIds.length * 2;
            if (newSize == 0) newSize = 10;
            int[] larger = new int[newSize];
            for (int i = 0; i < bookingIds.length; i++) {
                larger[i] = bookingIds[i];
            }
            bookingIds = larger;
        }
        bookingIds[bookingCount] = bookingId;
        bookingCount++;
    }

    /*
     * Allows adding multiple booking IDs at once using the SAME method name.
     */
    public void addBookingId(int... multipleBookingIds) {
        if (multipleBookingIds == null) return;
        for (int id : multipleBookingIds) {
            addBookingId(id); // Reuses the single logic
        }
    }

    /**
     * Remove a booking id if present. Returns true if removed.
     */
    public boolean removeBookingId(int bookingId) {
        for (int i = 0; i < bookingCount; i++) {
            if (bookingIds[i] == bookingId) {
                // shift left remaining elements
                for (int j = i; j < bookingCount - 1; j++) {
                    bookingIds[j] = bookingIds[j + 1];
                }
                bookingCount--;
                return true;
            }
        }
        return false;
    }

    public int getBookingCount() {
        return bookingCount;
    }

    /**
     * Return a copy of the valid portion of bookingIds array.
     */
    public int[] getBookingIds() {
        int[] copy = new int[bookingCount];
        for (int i = 0; i < bookingCount; i++) {
            copy[i] = bookingIds[i];
        }
        return copy;
    }

    @Override
    public String toString() {
        String idStr = (userId == null) ? "unassigned" : String.valueOf(userId);
        return "User[id=" + idStr + ", username=" + username + ", role=" + role + "]";
    }
}
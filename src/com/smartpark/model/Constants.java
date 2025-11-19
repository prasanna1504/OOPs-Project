package com.smartpark.model;

/*
 * Stores all constant values used throughout the Smart Parking System.
 * This class acts as a central configuration file for roles, slot types, 
 * status flags, billing rates, and system timeouts.
 */
public final class Constants {

    // Private constructor prevents instantiation of this utility class
    private Constants() { }

    /*
     * USER ROLES
     * Defines the hierarchy of access within the application.
    */
    
    public static final String ROLE_ADMIN = "ADMIN";          // Full system access (Infrastructure, Files)
    public static final String ROLE_ATTENDANT = "ATTENDANT";  // Operational access (Gates, Payments)
    public static final String ROLE_USER = "USER";            // Customer access (Reservations, History)

    /* PARKING SLOT TYPES
     * Categorizes the physical spaces available in the lot.
     */
    public static final String SLOT_COMPACT = "COMPACT";
    public static final String SLOT_REGULAR = "REGULAR";
    public static final String SLOT_LARGE = "LARGE";
    public static final String SLOT_HANDICAPPED = "HANDICAPPED";

    /* BOOKING STATUS FLAGS
     * Tracks the lifecycle of a vehicle's stay.
     */
    public static final String STATUS_PENDING = "PENDING";     // User reserved, but hasn't arrived yet
    public static final String STATUS_ACTIVE = "ACTIVE";       // Vehicle is currently parked inside
    public static final String STATUS_CANCELLED = "CANCELLED"; // Booking expired or was manually cancelled
    public static final String STATUS_COMPLETED = "COMPLETED"; // Vehicle exited and payment processed

    /*
     * BILLING RATES (PER MINUTE)
     * Fees are calculated by multiplying the duration in minutes by this rate.
     */
    public static final double RATE_COMPACT_FEE = 7.0;       // Cost per minute for compact cars
    public static final double RATE_REGULAR_FEE = 10.0;      // Cost per minute for regular cars
    public static final double RATE_LARGE_FEE = 20.0;        // Cost per minute for large vehicles
    public static final double RATE_HANDICAPPED_FEE = 5.0;   // Discounted rate per minute

    /*
     * SYSTEM CONFIGURATION
     * File paths and logic thresholds.
     */
    
    // The filename used for persisting booking data
    public static final String FILE_BOOKINGS_TEXT = "bookings.txt";

    // Auto-Expiration Timeout: 1 Minute (in milliseconds)
    public static final long BOOKING_TIMEOUT_MS = 60000;
}
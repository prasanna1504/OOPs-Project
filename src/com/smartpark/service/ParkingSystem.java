package com.smartpark.service;

import com.smartpark.interfaces.*;
import com.smartpark.model.*;
import com.smartpark.exceptions.SlotNotAvailableException;

import java.io.IOException;

/*
 * Core manager class for the Smart Parking System.
 * Handles slot management, booking creation, and attendant operations.
 */
public class ParkingSystem {
	
    private ParkingSlot[] parkingSlots; // Array storage for slots
    private int slotCount;

    private Booking[] bookings;         // Array storage for bookings
    private int bookingCount;

    private Integer nextBookingId;      // Auto-incrementing ID for bookings

    private Attendant attendant;        // Inner class instance handling operations

    /*
     * Constructor with custom capacity.
     */
    public ParkingSystem(int initialSlotCapacity, int initialBookingCapacity) {
        if (initialSlotCapacity <= 0) initialSlotCapacity = 20;
        if (initialBookingCapacity <= 0) initialBookingCapacity = 100;

        this.parkingSlots = new ParkingSlot[initialSlotCapacity];
        this.slotCount = 0;

        this.bookings = new Booking[initialBookingCapacity];
        this.bookingCount = 0;

        this.nextBookingId = 1;
        this.attendant = new Attendant();
    }

    /*
     * Default constructor.
     */
    public ParkingSystem() {
        this(20, 100);
    }

    /* NESTED CLASS: ParkingSlot (Static)
     * Represents a physical space in the parking lot.
     */
    public static class ParkingSlot {
        private int id;
        private String type;             // e.g., COMPACT, REGULAR
        private boolean occupied;        // Tracker for availability
        private Integer currentBookingId;// Reference to the active booking

        public ParkingSlot(int id, String type) {
            this.id = id;
            this.type = type;
            this.occupied = false;
            this.currentBookingId = null;
        }

        public int getId() { return id; }
        public String getType() { return type; }
        public boolean isOccupied() { return occupied; }

        // Mark the slot as occupied by a specific booking
        public void assign(Integer bookingId) {
            this.occupied = true;
            this.currentBookingId = bookingId;
        }

        // Free up the slot
        public void release() {
            this.occupied = false;
            this.currentBookingId = null;
        }
        
        public Integer getCurrentBookingId() {
            return currentBookingId;
        }
    }

    /*
     * INNER CLASS: Attendant
     * Handles operational tasks: Entry, Exit, and Payments.
     * Implements multiple interfaces (EntryExitHandler, Payment).
     */
    public class Attendant implements EntryExitHandler, Payment {

        /*
         * Marks the vehicle entry.
         * Updates status to ACTIVE and records the Entry Timestamp.
         */
    	public void markEntry(Booking booking) {
    	    if (booking == null) {
    	        System.out.println("[Error] Cannot mark entry: Booking is null.");
    	        return;
    	    }

    	    // Validate booking status - can only mark entry for PENDING bookings
    	    String currentStatus = booking.getStatus();
    	    if (!Constants.STATUS_PENDING.equals(currentStatus)) {
    	        System.out.println("[Error] Cannot mark entry for Booking ID " + booking.getBookingId() + 
    	                         ". Current status: " + currentStatus + ". Only PENDING bookings can be marked as entry.");
    	        return;
    	    }

    	    // 1. Set status to ACTIVE (Vehicle is now inside)
    	    booking.setStatus(Constants.STATUS_ACTIVE);
    	    
    	    // 2. Capture Entry Time (Critical for billing)
    	    long entryTimestamp = System.currentTimeMillis();
    	    booking.setEntryTime(entryTimestamp);

    	    // 3. Ensure slot exists and is properly assigned
    	    int slotId = booking.getSlotId();
    	    ParkingSlot slot = findSlotById(slotId);
    	    
    	    if (slot == null) {
    	        System.out.println("[Error] Slot ID " + slotId + " not found during markEntry for Booking ID " + booking.getBookingId());
    	        // Revert status change if slot doesn't exist
    	        booking.setStatus(Constants.STATUS_PENDING);
    	        return;
    	    }
    	    
    	    // Ensure slot is marked as occupied (should already be from reservation, but verify)
    	    if (!slot.isOccupied()) {
    	        slot.assign(booking.getBookingId());
    	    }
    	}

        /*
         * Marks the vehicle exit.
         * Updates status to COMPLETED, records Exit Timestamp, and calculates DURATION-BASED Fee.
         * Fee is calculated PER MINUTE.
         * Only works on ACTIVE bookings (vehicles that have entered).
         */
        public void markExit(Booking booking) {
            if (booking == null) {
                System.out.println("[Error] Cannot mark exit: Booking is null.");
                return;
            }

            // Validate booking status - can only mark exit for ACTIVE bookings
            String currentStatus = booking.getStatus();
            if (!Constants.STATUS_ACTIVE.equals(currentStatus)) {
                System.out.println("[Error] Cannot mark exit for Booking ID " + booking.getBookingId() + 
                                 ". Current status: " + currentStatus + ". Only ACTIVE bookings can be marked as exit.");
                return;
            }

            // Validate that entry was actually marked (entryTime should be set)
            long entryTimestamp = booking.getEntryTime();
            if (entryTimestamp <= 0) {
                System.out.println("[Error] Cannot mark exit for Booking ID " + booking.getBookingId() + 
                                 ". Entry time was never recorded. Please mark entry first.");
                return;
            }

            // 1. Capture Exit Time
            long exitTimestamp = System.currentTimeMillis();
            booking.setExitTime(exitTimestamp);

            // 2. Validate exit time is after entry time
            if (exitTimestamp < entryTimestamp) {
                System.out.println("[Error] Exit time cannot be before entry time for Booking ID " + booking.getBookingId());
                booking.setExitTime(0); // Reset exit time
                return;
            }

            // 3. Calculate Duration in Minutes
            long durationInMillis = exitTimestamp - entryTimestamp;
            // Convert milliseconds to minutes, rounding up
            double minutesParked = Math.ceil(durationInMillis / (1000.0 * 60));
            
            // Ensure at least 1 minute is charged 
            if (minutesParked < 1.0) minutesParked = 1.0;

            // 4. Determine Per-Minute Rate based on Slot Type
            int slotId = booking.getSlotId();
            ParkingSlot slot = findSlotById(slotId);
            double perMinuteRate = Constants.RATE_REGULAR_FEE; // Default fallback

            if (slot != null) {
                String type = slot.getType();
                if (Constants.SLOT_COMPACT.equals(type)) perMinuteRate = Constants.RATE_COMPACT_FEE;
                else if (Constants.SLOT_REGULAR.equals(type)) perMinuteRate = Constants.RATE_REGULAR_FEE;
                else if (Constants.SLOT_LARGE.equals(type)) perMinuteRate = Constants.RATE_LARGE_FEE;
                else if (Constants.SLOT_HANDICAPPED.equals(type)) perMinuteRate = Constants.RATE_HANDICAPPED_FEE;
            } else {
                System.out.println("[Warning] Slot ID " + slotId + " not found for Booking ID " + booking.getBookingId() + 
                                 ". Using default rate: $" + perMinuteRate + "/min");
            }

            // 5. Final Calculation: Minutes * Rate
            double totalAmount = minutesParked * perMinuteRate;
            booking.setAmount(Double.valueOf(totalAmount));

            // 6. Set status to COMPLETED
            booking.setStatus(Constants.STATUS_COMPLETED);

            // 7. Free the physical parking slot
            if (slot != null) {
                slot.release();
            } else {
                System.out.println("[Warning] Could not release slot " + slotId + " - slot not found.");
            }
        }

        /**
         * Process payment for a completed booking.
         * Validates that the booking is in a payable state and amount is sufficient.
         * Returns true if payment succeeds, false otherwise.
         */
        public boolean pay(Booking booking, Double amount) {
            if (booking == null || amount == null) {
                System.out.println("[Error] Payment failed: Booking or amount is null.");
                return false;
            }
            
            if (amount.doubleValue() < 0) {
                System.out.println("[Error] Payment failed: Amount cannot be negative.");
                return false;
            }
            
            // Only allow payment for COMPLETED bookings
            String status = booking.getStatus();
            if (!Constants.STATUS_COMPLETED.equals(status)) {
                System.out.println("[Error] Payment failed: Booking ID " + booking.getBookingId() + 
                                 " is not in COMPLETED status. Current status: " + status);
                return false;
            }
            
            Double due = booking.getAmount();
            
            if (due == null) {
                // If amount wasn't calculated yet, accept the payment amount
                booking.setAmount(amount);
                return true;
            }
            
            boolean paymentAccepted = amount.doubleValue() >= due.doubleValue();
            if (!paymentAccepted) {
                System.out.println("[Error] Payment failed: Amount $" + String.format("%.2f", amount) + 
                                 " is less than due amount $" + String.format("%.2f", due));
            }
            return paymentAccepted;
        }

        /**
         * Check if a refund can be processed for a booking.
         * Returns true if refund is possible, false otherwise.
         * Note: This validates refund eligibility but doesn't actually process the refund.
         */
        public boolean refund(Booking booking, Double amount) {
            if (booking == null || amount == null) {
                System.out.println("[Error] Refund check failed: Booking or amount is null.");
                return false;
            }
            
            if (amount.doubleValue() < 0) {
                System.out.println("[Error] Refund check failed: Amount cannot be negative.");
                return false;
            }
            
            // Only allow refunds for COMPLETED bookings
            String status = booking.getStatus();
            if (!Constants.STATUS_COMPLETED.equals(status)) {
                System.out.println("[Error] Refund not allowed: Booking ID " + booking.getBookingId() + 
                                 " is not in COMPLETED status. Current status: " + status);
                return false;
            }
            
            return true;
        }
    }

    /*
     * Cleanup method to auto-cancel bookings that have timed out.
     * Should be called before performing operations like showing slots or reserving.
     */
    public void processExpirations() {
        long currentTime = System.currentTimeMillis();

        for (int i = 0; i < bookingCount; i++) {
            Booking booking = bookings[i];
            
            // We only check bookings that are still PENDING (user hasn't arrived yet)
            if (booking != null && Constants.STATUS_PENDING.equals(booking.getStatus())) {
                
                long creationTime = booking.getCreationTime();
                long ageInMillis = currentTime - creationTime;

                // Check if the booking is older than the allowed timeout (i.e., 1 min)
                if (ageInMillis > Constants.BOOKING_TIMEOUT_MS) {
                    
                    // 1. Change status to CANCELLED
                    booking.setStatus(Constants.STATUS_CANCELLED);
                    
                    // 2. Free the slot immediately
                    ParkingSlot slot = findSlotById(booking.getSlotId());
                    if (slot != null) {
                        slot.release();
                    }
                    
                    System.out.println("[System] Booking ID " + booking.getBookingId() + " expired and was auto-cancelled.");
                }
            }
        }
    }

    /*
     * Add a single parking slot. Resizes array if necessary.
     */
    public void addSlot(String type) {
        if (slotCount == parkingSlots.length) {
            ParkingSlot[] larger = new ParkingSlot[parkingSlots.length * 2];
            for (int i = 0; i < parkingSlots.length; i++) larger[i] = parkingSlots[i];
            parkingSlots = larger;
        }
        int id = slotCount + 1;
        parkingSlots[slotCount] = new ParkingSlot(id, type);
        slotCount++;
    }

    /*
     * Vararg overloading: Add multiple slots at once.
     */
    public void addSlot(String... types) {
        if (types == null) return;
        for (String t : types) {
            addSlot(t);
        }
    }

    public ParkingSlot findSlotById(int id) {
        for (int i = 0; i < slotCount; i++) {
            ParkingSlot s = parkingSlots[i];
            if (s != null && s.getId() == id) return s;
        }
        return null;
    }

    /*
     * Reserve a specific slot by ID.
     * Validates slot existence and availability before creating the booking.
     */
    public Booking reserveSlot(int slotId, User user) throws SlotNotAvailableException {
        if (user == null) {
            throw new SlotNotAvailableException("Cannot reserve slot: User is null.");
        }
        
        ParkingSlot slot = findSlotById(slotId);
        
        if (slot == null) {
            throw new SlotNotAvailableException("Slot id " + slotId + " does not exist.");
        }
        if (slot.isOccupied()) {
            throw new SlotNotAvailableException("Slot id " + slotId + " is already occupied.");
        }

        // Create new booking
        Booking newBooking = new Booking(user.getUsername(), slotId);
        
        // Note: Creation time is set inside the Booking constructor now
        newBooking.setStatus(Constants.STATUS_PENDING);
        newBooking.setBookingId(nextBookingId);
        nextBookingId++;

        // Store booking (resize array if necessary)
        if (bookingCount == bookings.length) {
            Booking[] larger = new Booking[bookings.length * 2];
            for (int i = 0; i < bookings.length; i++) larger[i] = bookings[i];
            bookings = larger;
        }
        bookings[bookingCount] = newBooking;
        bookingCount++;

        // Mark slot as physically assigned (reserved for this booking)
        slot.assign(newBooking.getBookingId());

        // Link booking to user
        user.addBookingId(newBooking.getBookingId());

        return newBooking;
    }

    /*
     * Overloaded reserve method: Find any available slot for the user.
     * Automatically selects the first available slot.
     */
    public Booking reserveSlot(String username, User user) throws SlotNotAvailableException {
        if (user == null) {
            throw new SlotNotAvailableException("Cannot reserve slot: User is null.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new SlotNotAvailableException("Cannot reserve slot: Invalid username.");
        }
        
        for (int i = 0; i < slotCount; i++) {
            ParkingSlot slot = parkingSlots[i];
            
            if (slot != null && !slot.isOccupied()) {
                Booking newBooking = new Booking(username, slot.getId());
                newBooking.setStatus(Constants.STATUS_PENDING);
                newBooking.setBookingId(nextBookingId);
                nextBookingId++;

                // Store booking (resize array if necessary)
                if (bookingCount == bookings.length) {
                    Booking[] larger = new Booking[bookings.length * 2];
                    for (int j = 0; j < bookings.length; j++) larger[j] = bookings[j];
                    bookings = larger;
                }
                bookings[bookingCount] = newBooking;
                bookingCount++;

                // Mark slot as physically assigned (reserved for this booking)
                slot.assign(newBooking.getBookingId());
                
                // Link booking to user
                user.addBookingId(newBooking.getBookingId());
                
                return newBooking;
            }
        }
        throw new SlotNotAvailableException("No free slot available for reservation.");
    }
    
    /*
     * Find a booking object by its ID.
     * Required by Main.java to locate bookings for Entry/Exit operations.
     */
    public Booking findBookingById(Integer bookingId) {
        if (bookingId == null) return null;
        for (int i = 0; i < bookingCount; i++) {
            Booking b = bookings[i];
            if (b != null && b.getBookingId() != null && b.getBookingId().equals(bookingId)) {
                return b;
            }
        }
        return null;
    }

    public void saveBookingsToFile(String filename) {
        try {
            com.smartpark.util.Persistence.saveBookings(this.bookings, this.bookingCount, filename);
        } catch (IOException e) {
            System.out.println("Error saving bookings: " + e.getMessage());
        }
    }

    public void loadBookingsFromFile(String filename) {
        try {
            Booking[] loaded = com.smartpark.util.Persistence.loadBookings(this.bookings.length, filename);
            this.bookings = loaded;
            this.bookingCount = com.smartpark.util.Persistence.getLastLoadedBookingCount();

            int maxId = 0;
            for (int i = 0; i < bookingCount; i++) {
                Booking booking = bookings[i];
                if (booking != null) {
                    if (booking.getBookingId() != null) {
                        int id = booking.getBookingId().intValue();
                        if (id > maxId) maxId = id;
                    }
                    // Sync slot status: If PENDING or ACTIVE, mark slot occupied
                    String status = booking.getStatus();
                    if (Constants.STATUS_PENDING.equals(status) || Constants.STATUS_ACTIVE.equals(status)) {
                        ParkingSlot slot = findSlotById(booking.getSlotId());
                        if (slot != null) {
                            slot.assign(booking.getBookingId());
                        }
                    }
                }
            }
            this.nextBookingId = Integer.valueOf(maxId + 1);
        } catch (IOException e) {
            System.out.println("Error loading bookings: " + e.getMessage());
        }
    }

    public int getSlotCount() { return slotCount; }
    public Attendant getAttendant() { return attendant; }
    
    public ParkingSlot[] getSlotsArray() {
        ParkingSlot[] copy = new ParkingSlot[this.slotCount];
        for(int i = 0; i < this.slotCount; i++) copy[i] = this.parkingSlots[i];
        return copy;
    }
}
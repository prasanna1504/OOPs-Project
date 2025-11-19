package com.smartpark.interfaces;

import com.smartpark.model.Booking;

/*
 * Interface defining actions related to marking
 * the entry and exit of vehicles into the parking area.
 */
public interface EntryExitHandler {

    /*
     * Mark the entry of a vehicle associated with a booking.
     */
    void markEntry(Booking booking);

    /*
     * Mark the exit of a vehicle associated with a booking.
     */
    void markExit(Booking booking);
}

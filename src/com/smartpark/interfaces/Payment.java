package com.smartpark.interfaces;

import com.smartpark.model.Booking;

/*
 * An interface for payment operations.
 * Implemented later by the Attendant inner class.
 */
public interface Payment {

    /*
     * Process payment for a completed booking.
     * Returns true if the payment succeeds.
     */
    boolean pay(Booking booking, Double amount);

    /**
     * Refund an amount for a given booking.
     * Returns true if the refund succeeds.
     */
    boolean refund(Booking booking, Double amount);
}

package com.smartpark.exceptions;

/*
 * Custom checked exception used when a parking slot cannot be reserved
 * because it is already occupied or otherwise unavailable.
 */
public class SlotNotAvailableException extends Exception {

    // Default error message
    public SlotNotAvailableException() {
        super("Requested parking slot is not available.");
    }

    // Custom error message if needed
    public SlotNotAvailableException(String message) {
        super(message);
    }
}

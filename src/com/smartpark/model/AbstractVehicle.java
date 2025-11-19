package com.smartpark.model;

/*
 * Abstract base class for different vehicle types.
 * - Demonstrates an abstract class
 * - Provides overloaded constructors
 */
public abstract class AbstractVehicle {
    private String licensePlate;
    private String ownerName;

    /*
     * Constructor: create vehicle with license plate only.
     * Owner name will be set to an empty string.
     */
    public AbstractVehicle(String licensePlate) {
        this.licensePlate = licensePlate;
        this.ownerName = "";
    }

    /*
     * Overloaded constructor: create vehicle with license plate and owner name.
     */
    public AbstractVehicle(String licensePlate, String ownerName) {
        this.licensePlate = licensePlate;
        this.ownerName = ownerName;
    }

    /*
     * Return the vehicle type as a String.
     * This is abstract so subclasses must provide their specific type string.
     */
    public abstract String getType();
    
    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
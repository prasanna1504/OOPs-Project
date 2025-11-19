package com.smartpark.model;

/*
 * A simple Motorcycle class extending AbstractVehicle.
 * Demonstrates hierarchical inheritance together with Car.
 */
public class Motorcycle extends AbstractVehicle {

    private boolean hasStorageBox;

    /**
     * Constructor using only license plate.
     */
    public Motorcycle(String licensePlate) {
        super(licensePlate);
        this.hasStorageBox = false;
    }

    /**
     * Overloaded constructor: license plate + owner name.
     */
    public Motorcycle(String licensePlate, String ownerName) {
        super(licensePlate, ownerName);
        this.hasStorageBox = false;
    }

    /**
     * Required by the AbstractVehicle parent class.
     */
    @Override
    public String getType() {
        return "Motorcycle";
    }

    public boolean getHasStorageBox() {
        return hasStorageBox;
    }

    public void setHasStorageBox(boolean hasStorageBox) {
        this.hasStorageBox = hasStorageBox;
    }
}

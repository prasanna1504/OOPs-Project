package com.smartpark.model;

/*
 * A simple Car class extending AbstractVehicle.
 * Demonstrates hierarchical inheritance.
 */
public class Car extends AbstractVehicle {

    private int numberOfSeats;

    /*
     * Constructor using only license plate.
     */
    public Car(String licensePlate) {
        super(licensePlate);
        this.numberOfSeats = 4;
    }

    /**
     * Overloaded constructor: license plate + owner name.
     */
    public Car(String licensePlate, String ownerName) {
        super(licensePlate, ownerName);
        this.numberOfSeats = 4;
    }

    /**
     * Return the type of this vehicle as required by the abstract parent class.
     */
    @Override
    public String getType() {
        return "Car";
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }
}

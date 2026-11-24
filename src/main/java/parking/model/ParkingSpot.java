/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking.model;

import parking.exception.SpotOccupiedException;
import parking.exception.SpotTypeMismatchException;

public abstract class ParkingSpot {
    private final String spotId;
    private final SpotType type;
    private Vehicle currentVehicle;

    protected ParkingSpot(String spotId, SpotType type) {
        this.spotId = spotId;
        this.type = type;
    }

    public String getSpotId() { return spotId; }
    public SpotType getType() { return type; }
    public boolean isOccupied() { return currentVehicle != null; }
    public boolean isAvailable() { return !isOccupied(); }
    public Vehicle getCurrentVehicle() { return currentVehicle; }

    public boolean canPark(Vehicle vehicle) {
        return vehicle != null && vehicle.canParkIn(this);
    }

    public void park(Vehicle vehicle) {
        if (isOccupied()) {
            throw new SpotOccupiedException("车位 " + spotId + " 已被占用");
        }
        if (!canPark(vehicle)) {
            throw new SpotTypeMismatchException("车辆 " + vehicle.getPlateNumber() + " 不能停 " + type + " 车位 " + spotId);
        }
        this.currentVehicle = vehicle;
    }

    public void release() { 
        this.currentVehicle = null; 
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking.model;

public class Truck extends Vehicle {
    public Truck(String plateNumber) { 
        super(plateNumber, VehicleType.TRUCK); 
    }

    @Override
    public boolean canParkIn(ParkingSpot spot) {
        return spot.getType() == SpotType.LARGE;
    }

    @Override
    public SpotType getPreferredSpotType() { 
        return SpotType.LARGE; 
    }
}

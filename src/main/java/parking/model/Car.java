/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking.model;

public class Car extends Vehicle {
    public Car(String plateNumber) { 
        super(plateNumber, VehicleType.CAR); 
    }

    @Override
    public boolean canParkIn(ParkingSpot spot) {
        return spot.getType() == SpotType.COMPACT;
    }

    @Override
    public SpotType getPreferredSpotType() { 
        return SpotType.COMPACT; 
    }
}
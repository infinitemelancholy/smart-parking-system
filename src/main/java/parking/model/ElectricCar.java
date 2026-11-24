/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking.model;

public class ElectricCar extends Vehicle {
    public ElectricCar(String plateNumber) { 
        super(plateNumber, VehicleType.ELECTRIC_CAR); 
    }

    @Override
    public boolean canParkIn(ParkingSpot spot) {
        return spot.getType() == SpotType.EV || spot.getType() == SpotType.COMPACT;
    }

    @Override
    public SpotType getPreferredSpotType() { 
        return SpotType.EV; 
    }
}
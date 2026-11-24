/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking.model;

public abstract class Vehicle {
    private final String plateNumber;
    private final VehicleType type;

    protected Vehicle(String plateNumber, VehicleType type) {
        if (plateNumber == null || plateNumber.isBlank()) {
            throw new IllegalArgumentException("车牌号不能为空");
        }
        this.plateNumber = plateNumber;
        this.type = type;
    }

    public String getPlateNumber() { return plateNumber; }
    public VehicleType getType() { return type; }

    public abstract boolean canParkIn(ParkingSpot spot);
    public abstract SpotType getPreferredSpotType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle other)) return false;
        return plateNumber.equals(other.plateNumber);
    }

    @Override
    public int hashCode() { return plateNumber.hashCode(); }
}
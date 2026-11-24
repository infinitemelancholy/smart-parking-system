/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking.model;

public class EvSpot extends ParkingSpot {
    public EvSpot(String spotId) { 
        super(spotId, SpotType.EV); 
    }
}
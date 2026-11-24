/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking.model;

import java.time.LocalDateTime;

public class ParkingRecord {
    private final String ticketId;
    private final String plateNumber;
    private final String spotId;
    private final LocalDateTime entryTime;
    private final LocalDateTime exitTime;
    private final double fee;
    private final FeeType feeType;

    public ParkingRecord(String ticketId, String plateNumber, String spotId, LocalDateTime entryTime, LocalDateTime exitTime, double fee, FeeType feeType) {
        this.ticketId = ticketId;
        this.plateNumber = plateNumber;
        this.spotId = spotId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.fee = fee;
        this.feeType = feeType;
    }

    public String getTicketId() { return ticketId; }
    public String getPlateNumber() { return plateNumber; }
    public String getSpotId() { return spotId; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public double getFee() { return fee; }
    public FeeType getFeeType() { return feeType; }
}
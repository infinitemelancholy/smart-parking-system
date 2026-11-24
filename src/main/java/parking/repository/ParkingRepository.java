package parking.repository;
import java.util.List;
import java.util.Optional;
import parking.model.ParkingRecord;
import parking.model.ParkingSpot;
import parking.model.Ticket;
public interface ParkingRepository{
    void saveActiveTicket(Ticket ticket);
    Optional<Ticket> findActiveTicketByPlate(String plateNumber);
    void removeActiveTicket(String plateNumber);
    void saveRecord(ParkingRecord record);
    List<ParkingRecord> findAllRecords();
    void saveSpots(List<ParkingSpot> spots);
    List<ParkingSpot> loadSpots();
    default List<Ticket> findAllActiveTickets(){
        return List.of();
    }
}

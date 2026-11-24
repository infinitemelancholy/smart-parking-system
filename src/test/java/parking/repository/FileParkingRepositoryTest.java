package parking.repository;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import parking.exception.RepositoryException;
import parking.model.Car;
import parking.model.CompactSpot;
import parking.model.ElectricCar;
import parking.model.EvSpot;
import parking.model.FeeType;
import parking.model.LargeSpot;
import parking.model.ParkingRecord;
import parking.model.ParkingSpot;
import parking.model.SpotType;
import parking.model.Ticket;
import parking.model.VehicleType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class FileParkingRepositoryTest{
    @TempDir
    Path tempDir;
    private Path dataDirectory;
    private FileParkingRepository repository;
    @BeforeEach
    void setUp(){
        dataDirectory=tempDir.resolve("data");
        repository=new FileParkingRepository(dataDirectory);
    }
    @Test
    void createsDataFilesOnStartup(){
        assertTrue(Files.exists(dataDirectory.resolve("active_tickets.txt")));
        assertTrue(Files.exists(dataDirectory.resolve("records.txt")));
        assertTrue(Files.exists(dataDirectory.resolve("spots.txt")));
    }
    @Test
    void savesAndFindsActiveTicketByPlate(){
        Ticket ticket=newTicket("T001","B12345","C01");
        repository.saveActiveTicket(ticket);
        Optional<Ticket> found=repository.findActiveTicketByPlate("B12345");
        assertTrue(found.isPresent());
        assertEquals("T001",found.get().getTicketId());
        assertEquals(VehicleType.CAR,found.get().getVehicle().getType());
        assertEquals(SpotType.COMPACT,found.get().getSpot().getType());
        assertEquals(ticket.getEntryTime(),found.get().getEntryTime());
    }
    @Test
    void updatesTicketWhenSamePlateIsSavedTwice(){
        repository.saveActiveTicket(newTicket("T001","B12345","C01"));
        repository.saveActiveTicket(newTicket("T002","B12345","C02"));
        List<Ticket> tickets=repository.findAllActiveTickets();
        assertEquals(1,tickets.size());
        assertEquals("T002",repository.findActiveTicketByPlate("B12345").orElseThrow().getTicketId());
    }
    @Test
    void removesActiveTicket(){
        repository.saveActiveTicket(newTicket("T001","B12345","C01"));
        repository.removeActiveTicket("B12345");
        assertTrue(repository.findActiveTicketByPlate("B12345").isEmpty());
    }
    @Test
    void savesAndReadsParkingRecords(){
        repository.saveRecord(new ParkingRecord("T001","B12345","C01",LocalDateTime.of(2026,11,24,9,0),LocalDateTime.of(2026,11,24,11,30),12.5,FeeType.HOURLY));
        repository.saveRecord(new ParkingRecord("T002","B88888","E01",LocalDateTime.of(2026,11,24,10,0),LocalDateTime.of(2026,11,24,12,0),0,FeeType.MEMBER));
        List<ParkingRecord> records=repository.findAllRecords();
        assertEquals(2,records.size());
        assertEquals("B12345",records.get(0).getPlateNumber());
        assertEquals("C01",records.get(0).getSpotId());
        assertEquals(12.5,records.get(0).getFee(),0.001);
        assertEquals(FeeType.HOURLY,records.get(0).getFeeType());
        assertEquals(LocalDateTime.of(2026,11,24,11,30),records.get(0).getExitTime());
    }
    @Test
    void savesAndRestoresSpotStates(){
        List<ParkingSpot> spots=List.of(new CompactSpot("C01"),new LargeSpot("L01"),new EvSpot("E01"));
        spots.get(2).park(new ElectricCar("B88888"));
        repository.saveSpots(spots);
        List<ParkingSpot> loaded=repository.loadSpots();
        assertEquals(3,loaded.size());
        assertEquals(SpotType.COMPACT,loaded.get(0).getType());
        assertTrue(loaded.get(0).isAvailable());
        assertTrue(loaded.get(1).isAvailable());
        assertTrue(loaded.get(2).isOccupied());
        assertEquals("B88888",loaded.get(2).getCurrentVehicle().getPlateNumber());
        assertEquals(VehicleType.ELECTRIC_CAR,loaded.get(2).getCurrentVehicle().getType());
    }
    @Test
    void corruptedSpotFileThrowsRepositoryException(){
        write("spots.txt","C01,UNKNOWN,0,,");
        assertThrows(RepositoryException.class,()->repository.loadSpots());
    }
    @Test
    void corruptedTicketFileThrowsRepositoryException(){
        write("active_tickets.txt","T001,B12345");
        assertThrows(RepositoryException.class,()->repository.findActiveTicketByPlate("B12345"));
    }
    @Test
    void missingFilesAreRecreated(){
        FileParkingRepository reopened=new FileParkingRepository(dataDirectory.resolve("nested"));
        assertTrue(Files.exists(dataDirectory.resolve("nested").resolve("spots.txt")));
        assertTrue(reopened.loadSpots().isEmpty());
        assertTrue(reopened.findAllRecords().isEmpty());
    }
    @Test
    void removingUnknownPlateKeepsExistingTickets(){
        repository.saveActiveTicket(newTicket("T001","B12345","C01"));
        repository.removeActiveTicket("B99999");
        assertFalse(repository.findAllActiveTickets().isEmpty());
    }
    private void write(String fileName,String content){
        try{
            Files.writeString(dataDirectory.resolve(fileName),content,StandardCharsets.UTF_8);
        }catch(Exception e){
            throw new IllegalStateException(e);
        }
    }
    private Ticket newTicket(String ticketId,String plateNumber,String spotId){
        Car car=new Car(plateNumber);
        CompactSpot spot=new CompactSpot(spotId);
        spot.park(car);
        return new Ticket(ticketId,car,spot,LocalDateTime.of(2026,11,24,9,0));
    }
}

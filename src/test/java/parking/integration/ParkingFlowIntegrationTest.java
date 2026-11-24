package parking.integration;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import parking.exception.NoAvailableSpotException;
import parking.exception.PaymentFailedException;
import parking.exception.VehicleAlreadyParkedException;
import parking.exception.VehicleNotParkedException;
import parking.model.Car;
import parking.model.CompactSpot;
import parking.model.EvSpot;
import parking.model.FeeType;
import parking.model.LargeSpot;
import parking.model.ParkingRecord;
import parking.model.ParkingSpot;
import parking.model.Ticket;
import parking.policy.FeeStrategyFactory;
import parking.policy.FirstFitSpotAllocationPolicy;
import parking.policy.HourlyFeeStrategy;
import parking.repository.FileParkingRepository;
import parking.repository.ParkingRepository;
import parking.service.ParkingLotService;
import parking.service.PaymentService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class ParkingFlowIntegrationTest{
    @TempDir
    Path tempDir;
    private Path dataDirectory;
    private FileParkingRepository repository;
    private List<ParkingSpot> spots;
    private ParkingLotService service;
    @BeforeEach
    void setUp(){
        dataDirectory=tempDir.resolve("data");
        repository=new FileParkingRepository(dataDirectory);
        spots=new ArrayList<>();
        spots.add(new CompactSpot("C01"));
        spots.add(new CompactSpot("C02"));
        spots.add(new LargeSpot("L01"));
        spots.add(new EvSpot("E01"));
        repository.saveSpots(spots);
        service=newService(repository,spots,amount->true);
    }
    @Test
    void parkAndExitUpdatesSpotsRecordsAndRevenue(){
        int availableBefore=service.getAvailableSpots().size();
        Ticket ticket=service.parkVehicle(new Car("B12345"));
        assertEquals("C01",ticket.getSpot().getSpotId());
        assertEquals(availableBefore-1,service.getAvailableSpots().size());
        assertTrue(service.findActiveTicket("B12345").isPresent());
        LocalDateTime exitTime=ticket.getEntryTime().plusHours(2).plusMinutes(30);
        double expectedFee=new HourlyFeeStrategy().calculateFee(ticket,exitTime);
        ParkingRecord record=service.exitVehicle("B12345",FeeType.HOURLY,exitTime);
        assertEquals(expectedFee,record.getFee(),0.001);
        assertEquals(1,service.getRecords().size());
        assertEquals(expectedFee,service.getTotalRevenue(),0.001);
        assertTrue(service.findActiveTicket("B12345").isEmpty());
        assertEquals(availableBefore,service.getAvailableSpots().size());
    }
    @Test
    void rejectsDuplicatePlate(){
        service.parkVehicle(new Car("B12345"));
        assertThrows(VehicleAlreadyParkedException.class,()->service.parkVehicle(new Car("B12345")));
    }
    @Test
    void rejectsWhenNoSpotIsAvailable(){
        ParkingLotService smallService=newService(repository,List.of(new CompactSpot("C99")),amount->true);
        smallService.parkVehicle(new Car("B11111"));
        assertThrows(NoAvailableSpotException.class,()->smallService.parkVehicle(new Car("B22222")));
    }
    @Test
    void rejectsExitOfUnknownVehicle(){
        assertThrows(VehicleNotParkedException.class,()->service.exitVehicle("B99999",FeeType.HOURLY,LocalDateTime.now()));
    }
    @Test
    void keepsVehicleParkedWhenPaymentFails(){
        Ticket ticket=service.parkVehicle(new Car("B12345"));
        ParkingLotService failingService=newService(repository,spots,amount->false);
        assertThrows(PaymentFailedException.class,()->failingService.exitVehicle("B12345",FeeType.HOURLY,ticket.getEntryTime().plusHours(1)));
        assertTrue(failingService.findActiveTicket("B12345").isPresent());
        assertEquals(3,failingService.getAvailableSpots().size());
    }
    @Test
    void restoresSpotsAndRecordsAfterRestart(){
        Ticket ticket=service.parkVehicle(new Car("B12345"));
        ParkingRecord record=service.exitVehicle("B12345",FeeType.DAILY,ticket.getEntryTime().plusHours(1));
        FileParkingRepository reopenedRepository=new FileParkingRepository(dataDirectory);
        List<ParkingSpot> restoredSpots=reopenedRepository.loadSpots();
        ParkingLotService restartedService=newService(reopenedRepository,restoredSpots,amount->true);
        assertEquals(1,restartedService.getRecords().size());
        assertEquals(record.getFee(),restartedService.getTotalRevenue(),0.001);
        assertEquals(4,restartedService.getAvailableSpots().size());
    }
    @Test
    void restoresActiveTicketsAfterRestart(){
        service.parkVehicle(new Car("B12345"));
        FileParkingRepository reopenedRepository=new FileParkingRepository(dataDirectory);
        List<ParkingSpot> restoredSpots=reopenedRepository.loadSpots();
        ParkingLotService restartedService=newService(reopenedRepository,restoredSpots,amount->true);
        assertEquals(1,restartedService.getActiveTickets().size());
        assertEquals(3,restartedService.getAvailableSpots().size());
        assertTrue(restartedService.findActiveTicket("B12345").isPresent());
    }
    private ParkingLotService newService(ParkingRepository parkingRepository,List<ParkingSpot> parkingSpots,PaymentService paymentService){
        return new ParkingLotService(parkingRepository,new FirstFitSpotAllocationPolicy(),new FeeStrategyFactory(),parkingSpots,paymentService);
    }
}

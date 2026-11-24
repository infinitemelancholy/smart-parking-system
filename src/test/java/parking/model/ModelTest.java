package parking.model;

import org.junit.jupiter.api.Test;
import parking.exception.SpotOccupiedException;
import parking.exception.SpotTypeMismatchException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void carCanParkInCompactSpot() {
        Car car = new Car("粤B12345");
        CompactSpot spot = new CompactSpot("C01");
        assertTrue(spot.canPark(car));
        spot.park(car);
        assertTrue(spot.isOccupied());
        assertEquals(car, spot.getCurrentVehicle());
    }

    @Test
    void truckCannotParkInCompactSpot() {
        Truck truck = new Truck("粤B99999");
        CompactSpot spot = new CompactSpot("C02");
        assertThrows(SpotTypeMismatchException.class, () -> spot.park(truck));
        assertTrue(spot.isAvailable());
    }

    @Test
    void electricCarCanParkInEvSpot() {
        ElectricCar ev = new ElectricCar("粤B88888");
        EvSpot spot = new EvSpot("E01");
        spot.park(ev);
        assertEquals(ev, spot.getCurrentVehicle());
    }

    @Test
    void carCannotParkInEvSpot() {
        Car car = new Car("粤B12345");
        EvSpot spot = new EvSpot("E02");
        assertThrows(SpotTypeMismatchException.class, () -> spot.park(car));
    }

    @Test
    void parkingOccupiedSpotThrows() {
        Car car1 = new Car("粤B11111");
        Car car2 = new Car("粤B22222");
        CompactSpot spot = new CompactSpot("C03");
        spot.park(car1);
        assertThrows(SpotOccupiedException.class, () -> spot.park(car2));
    }

    @Test
    void releaseMakesSpotAvailableAgain() {
        Car car = new Car("粤B11111");
        CompactSpot spot = new CompactSpot("C04");
        spot.park(car);
        spot.release();
        assertTrue(spot.isAvailable());
        assertNull(spot.getCurrentVehicle());
    }

    @Test
    void ticketCloseUpdatesStatus() {
        Car car = new Car("粤B11111");
        CompactSpot spot = new CompactSpot("C05");
        spot.park(car);
        LocalDateTime entry = LocalDateTime.of(2026, 1, 1, 8, 0);
        Ticket ticket = new Ticket("T001", car, spot, entry);

        assertTrue(ticket.isActive());
        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());

        LocalDateTime exit = LocalDateTime.of(2026, 1, 1, 10, 0);
        ticket.close(exit);

        assertFalse(ticket.isActive());
        assertEquals(TicketStatus.CLOSED, ticket.getStatus());
        assertEquals(exit, ticket.getExitTime());
    }

    @Test
    void duplicatePlateVehiclesAreEqual() {
        assertEquals(new Car("粤B12345"), new Car("粤B12345"));
        assertNotEquals(new Car("粤B12345"), new Car("粤B54321"));
    }

    @Test
    void blankPlateRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Car("  "));
    }
}
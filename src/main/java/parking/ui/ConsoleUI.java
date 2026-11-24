package parking.ui;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import parking.exception.InvalidInputException;
import parking.exception.RepositoryException;
import parking.model.Car;
import parking.model.ElectricCar;
import parking.model.FeeType;
import parking.model.Motorcycle;
import parking.model.ParkingRecord;
import parking.model.ParkingSpot;
import parking.model.Ticket;
import parking.model.Truck;
import parking.model.Vehicle;
import parking.model.VehicleType;
import parking.service.ParkingLotService;
import parking.util.DateTimeUtil;
public class ConsoleUI{
    private final ParkingLotService service;
    private final Scanner scanner;
    private final boolean debug;
    public ConsoleUI(ParkingLotService service){
        this(service,new Scanner(System.in));
    }
    public ConsoleUI(ParkingLotService service,Scanner scanner){
        this.service=service;
        this.scanner=scanner;
        this.debug=Boolean.getBoolean("parking.debug");
    }
    public void start(){
        System.out.println("===== Smart Parking System =====");
        boolean running=true;
        while(running){
            printMenu();
            try{
                int choice=InputValidator.readMenuOption(scanner,1,7);
                switch(choice){
                    case 1:
                        showAvailableSpots();
                        break;
                    case 2:
                        parkVehicle();
                        break;
                    case 3:
                        exitVehicle();
                        break;
                    case 4:
                        showActiveTickets();
                        break;
                    case 5:
                        showRecords();
                        break;
                    case 6:
                        showTotalRevenue();
                        break;
                    default:
                        running=false;
                        break;
                }
            }catch(InvalidInputException e){
                System.out.println("[Input error] "+e.getMessage());
            }catch(RepositoryException e){
                System.out.println("[Storage error] "+e.getMessage());
            }catch(RuntimeException e){
                System.out.println("[Error] "+messageOf(e));
                printDebug(e);
            }catch(Exception e){
                System.out.println("[System error] "+e.getMessage());
                printDebug(e);
            }
        }
        System.out.println("Goodbye.");
    }
    private void printMenu(){
        System.out.println();
        System.out.println("1. Show available spots");
        System.out.println("2. Park a vehicle");
        System.out.println("3. Exit a vehicle");
        System.out.println("4. Show parked vehicles");
        System.out.println("5. Show parking records");
        System.out.println("6. Show total revenue");
        System.out.println("7. Quit");
        System.out.print("Choose an option: ");
    }
    private void showAvailableSpots(){
        List<ParkingSpot> spots=service.getAvailableSpots();
        if(spots.isEmpty()){
            System.out.println("No available spot.");
            return;
        }
        System.out.println("Available spots: "+spots.size());
        for(ParkingSpot spot:spots){
            System.out.println("  "+spot.getSpotId()+"  "+spot.getType());
        }
    }
    private void parkVehicle(){
        System.out.print("Vehicle type (1 Car / 2 Motorcycle / 3 Truck / 4 Electric car): ");
        VehicleType vehicleType=InputValidator.readVehicleType(scanner);
        System.out.print("Plate number: ");
        String plateNumber=InputValidator.readPlate(scanner);
        Vehicle vehicle=createVehicle(plateNumber,vehicleType);
        Ticket ticket=service.parkVehicle(vehicle);
        System.out.println("Parked: ticket "+ticket.getTicketId()+", spot "+ticket.getSpot().getSpotId()+", entry "+DateTimeUtil.format(ticket.getEntryTime()));
    }
    private void exitVehicle(){
        System.out.print("Plate number: ");
        String plateNumber=InputValidator.readPlate(scanner);
        System.out.print("Fee type (1 Hourly / 2 Daily / 3 Member): ");
        FeeType feeType=InputValidator.readFeeType(scanner);
        System.out.print("Exit time (yyyy-MM-dd HH:mm, press Enter for now): ");
        LocalDateTime exitTime=InputValidator.readDateTime(scanner);
        ParkingRecord record=service.exitVehicle(plateNumber,feeType,exitTime);
        System.out.println("Exit completed: ticket "+record.getTicketId()+", spot "+record.getSpotId());
        System.out.println("Entry: "+DateTimeUtil.format(record.getEntryTime())+"   Exit: "+DateTimeUtil.format(record.getExitTime()));
        System.out.println("Fee: "+format(record.getFee())+" HKD ("+record.getFeeType()+")");
    }
    private void showActiveTickets(){
        List<Ticket> tickets=service.getActiveTickets();
        if(tickets.isEmpty()){
            System.out.println("No vehicle is currently parked.");
            return;
        }
        System.out.println("Parked vehicles: "+tickets.size());
        for(Ticket ticket:tickets){
            System.out.println("  "+ticket.getTicketId()+"  "+ticket.getVehicle().getPlateNumber()+"  "+ticket.getVehicle().getType()+"  spot "+ticket.getSpot().getSpotId()+"  entry "+DateTimeUtil.format(ticket.getEntryTime()));
        }
    }
    private void showRecords(){
        List<ParkingRecord> records=service.getRecords();
        if(records.isEmpty()){
            System.out.println("No parking record.");
            return;
        }
        System.out.println("Parking records: "+records.size());
        for(ParkingRecord record:records){
            System.out.println("  "+record.getTicketId()+"  "+record.getPlateNumber()+"  spot "+record.getSpotId()+"  "+DateTimeUtil.format(record.getEntryTime())+" -> "+DateTimeUtil.format(record.getExitTime())+"  "+format(record.getFee())+" HKD  "+record.getFeeType());
        }
    }
    private void showTotalRevenue(){
        System.out.println("Total revenue: "+format(service.getTotalRevenue())+" HKD");
    }
    private Vehicle createVehicle(String plateNumber,VehicleType vehicleType){
        switch(vehicleType){
            case CAR:
                return new Car(plateNumber);
            case MOTORCYCLE:
                return new Motorcycle(plateNumber);
            case TRUCK:
                return new Truck(plateNumber);
            case ELECTRIC_CAR:
                return new ElectricCar(plateNumber);
            default:
                throw new InvalidInputException("Unknown vehicle type.");
        }
    }
    private String format(double amount){
        return String.format(Locale.ROOT,"%.2f",amount);
    }
    private String messageOf(RuntimeException e){
        String message=e.getMessage();
        if(message==null||message.isBlank()){
            return e.getClass().getSimpleName();
        }
        return message;
    }
    private void printDebug(Exception e){
        if(debug){
            e.printStackTrace();
        }
    }
}

package parking.repository;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import parking.exception.RepositoryException;
import parking.model.Car;
import parking.model.CompactSpot;
import parking.model.ElectricCar;
import parking.model.EvSpot;
import parking.model.FeeType;
import parking.model.LargeSpot;
import parking.model.Motorcycle;
import parking.model.ParkingRecord;
import parking.model.ParkingSpot;
import parking.model.SpotType;
import parking.model.Ticket;
import parking.model.Truck;
import parking.model.Vehicle;
import parking.model.VehicleType;
import parking.util.DateTimeUtil;
public class FileParkingRepository implements ParkingRepository{
    private static final String DELIMITER=",";
    private final Path dataDirectory;
    private final Path activeTicketFile;
    private final Path recordFile;
    private final Path spotFile;
    public FileParkingRepository(Path dataDirectory){
        this.dataDirectory=dataDirectory;
        this.activeTicketFile=dataDirectory.resolve("active_tickets.txt");
        this.recordFile=dataDirectory.resolve("records.txt");
        this.spotFile=dataDirectory.resolve("spots.txt");
        initStorage();
    }
    private void initStorage(){
        try{
            Files.createDirectories(dataDirectory);
            createIfMissing(activeTicketFile);
            createIfMissing(recordFile);
            createIfMissing(spotFile);
        }catch(IOException e){
            throw new RepositoryException("Cannot initialize data directory: "+dataDirectory,e);
        }
    }
    private void createIfMissing(Path file) throws IOException{
        if(Files.notExists(file)){
            Files.createFile(file);
        }
    }
    @Override
    public void saveActiveTicket(Ticket ticket){
        if(ticket==null){
            throw new RepositoryException("Cannot save a null ticket.");
        }
        String plateNumber=ticket.getVehicle().getPlateNumber();
        List<String> lines=readLines(activeTicketFile);
        boolean updated=false;
        for(int i=0;i<lines.size();i++){
            if(plateNumber.equals(field(lines.get(i),1))){
                lines.set(i,toTicketLine(ticket));
                updated=true;
                break;
            }
        }
        if(!updated){
            lines.add(toTicketLine(ticket));
        }
        writeLines(activeTicketFile,lines);
    }
    @Override
    public Optional<Ticket> findActiveTicketByPlate(String plateNumber){
        if(plateNumber==null){
            return Optional.empty();
        }
        for(String line:readLines(activeTicketFile)){
            if(plateNumber.equals(field(line,1))){
                return Optional.of(parseTicket(line));
            }
        }
        return Optional.empty();
    }
    @Override
    public void removeActiveTicket(String plateNumber){
        List<String> lines=readLines(activeTicketFile);
        boolean removed=false;
        for(int i=lines.size()-1;i>=0;i--){
            if(plateNumber.equals(field(lines.get(i),1))){
                lines.remove(i);
                removed=true;
            }
        }
        if(removed){
            writeLines(activeTicketFile,lines);
        }
    }
    @Override
    public List<Ticket> findAllActiveTickets(){
        List<Ticket> tickets=new ArrayList<>();
        for(String line:readLines(activeTicketFile)){
            tickets.add(parseTicket(line));
        }
        return Collections.unmodifiableList(tickets);
    }
    @Override
    public void saveRecord(ParkingRecord record){
        if(record==null){
            throw new RepositoryException("Cannot save a null record.");
        }
        appendLine(recordFile,toRecordLine(record));
    }
    @Override
    public List<ParkingRecord> findAllRecords(){
        List<ParkingRecord> records=new ArrayList<>();
        for(String line:readLines(recordFile)){
            records.add(parseRecord(line));
        }
        return Collections.unmodifiableList(records);
    }
    @Override
    public void saveSpots(List<ParkingSpot> spots){
        List<String> lines=new ArrayList<>();
        for(ParkingSpot spot:spots){
            String plateNumber="";
            String vehicleType="";
            if(spot.isOccupied()){
                plateNumber=spot.getCurrentVehicle().getPlateNumber();
                vehicleType=spot.getCurrentVehicle().getType().name();
            }
            lines.add(String.join(DELIMITER,spot.getSpotId(),spot.getType().name(),spot.isOccupied()?"1":"0",plateNumber,vehicleType));
        }
        writeLines(spotFile,lines);
    }
    @Override
    public List<ParkingSpot> loadSpots(){
        List<ParkingSpot> spots=new ArrayList<>();
        for(String line:readLines(spotFile)){
            spots.add(parseSpot(line));
        }
        return spots;
    }
    private ParkingSpot parseSpot(String line){
        String[] fields=split(line);
        if(fields.length!=5){
            throw new RepositoryException("Invalid spot record: "+line);
        }
        try{
            ParkingSpot spot=createSpot(fields[0],SpotType.valueOf(fields[1]));
            if("1".equals(fields[2])){
                if(fields[3].isEmpty()||fields[4].isEmpty()){
                    throw new IllegalArgumentException("Occupied spot without vehicle");
                }
                spot.park(createVehicle(fields[3],VehicleType.valueOf(fields[4])));
            }
            return spot;
        }catch(RuntimeException e){
            throw new RepositoryException("Invalid spot record: "+line,e);
        }
    }
    private String toTicketLine(Ticket ticket){
        return String.join(DELIMITER,
                ticket.getTicketId(),
                ticket.getVehicle().getPlateNumber(),
                ticket.getVehicle().getType().name(),
                ticket.getSpot().getSpotId(),
                ticket.getSpot().getType().name(),
                DateTimeUtil.toStorage(ticket.getEntryTime()));
    }
    private Ticket parseTicket(String line){
        String[] fields=split(line);
        if(fields.length!=6){
            throw new RepositoryException("Invalid active ticket record: "+line);
        }
        try{
            Vehicle vehicle=createVehicle(fields[1],VehicleType.valueOf(fields[2]));
            ParkingSpot spot=createSpot(fields[3],SpotType.valueOf(fields[4]));
            spot.park(vehicle);
            return new Ticket(fields[0],vehicle,spot,DateTimeUtil.fromStorage(fields[5]));
        }catch(RuntimeException e){
            throw new RepositoryException("Invalid active ticket record: "+line,e);
        }
    }
    private String toRecordLine(ParkingRecord record){
        return String.join(DELIMITER,
                record.getTicketId(),
                record.getPlateNumber(),
                record.getSpotId(),
                DateTimeUtil.toStorage(record.getEntryTime()),
                DateTimeUtil.toStorage(record.getExitTime()),
                String.valueOf(record.getFee()),
                record.getFeeType().name());
    }
    private ParkingRecord parseRecord(String line){
        String[] fields=split(line);
        if(fields.length!=7){
            throw new RepositoryException("Invalid parking record: "+line);
        }
        try{
            return new ParkingRecord(fields[0],
                    fields[1],
                    fields[2],
                    DateTimeUtil.fromStorage(fields[3]),
                    DateTimeUtil.fromStorage(fields[4]),
                    Double.parseDouble(fields[5]),
                    FeeType.valueOf(fields[6]));
        }catch(RuntimeException e){
            throw new RepositoryException("Invalid parking record: "+line,e);
        }
    }
    private Vehicle createVehicle(String plateNumber,VehicleType type){
        switch(type){
            case CAR:
                return new Car(plateNumber);
            case MOTORCYCLE:
                return new Motorcycle(plateNumber);
            case TRUCK:
                return new Truck(plateNumber);
            case ELECTRIC_CAR:
                return new ElectricCar(plateNumber);
            default:
                throw new IllegalArgumentException("Unknown vehicle type: "+type);
        }
    }
    private ParkingSpot createSpot(String spotId,SpotType type){
        switch(type){
            case COMPACT:
                return new CompactSpot(spotId);
            case LARGE:
                return new LargeSpot(spotId);
            case EV:
                return new EvSpot(spotId);
            default:
                throw new IllegalArgumentException("Unknown spot type: "+type);
        }
    }
    private List<String> readLines(Path file){
        try{
            List<String> lines=new ArrayList<>();
            for(String line:Files.readAllLines(file,StandardCharsets.UTF_8)){
                if(!line.isBlank()){
                    lines.add(line);
                }
            }
            return lines;
        }catch(IOException e){
            throw new RepositoryException("Cannot read data file: "+file.getFileName(),e);
        }
    }
    private void writeLines(Path file,List<String> lines){
        Path tempFile=file.resolveSibling(file.getFileName()+".tmp");
        try{
            Files.write(tempFile,lines,StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            try{
                Files.move(tempFile,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
            }catch(AtomicMoveNotSupportedException e){
                Files.move(tempFile,file,StandardCopyOption.REPLACE_EXISTING);
            }
        }catch(IOException e){
            throw new RepositoryException("Cannot write data file: "+file.getFileName(),e);
        }
    }
    private void appendLine(Path file,String line){
        try(BufferedWriter writer=Files.newBufferedWriter(file,StandardCharsets.UTF_8,StandardOpenOption.APPEND)){
            writer.write(line);
            writer.newLine();
        }catch(IOException e){
            throw new RepositoryException("Cannot write data file: "+file.getFileName(),e);
        }
    }
    private String[] split(String line){
        String[] fields=line.split(DELIMITER,-1);
        for(int i=0;i<fields.length;i++){
            fields[i]=fields[i].trim();
        }
        return fields;
    }
    private String field(String line,int index){
        String[] fields=split(line);
        if(index>=fields.length){
            throw new RepositoryException("Invalid data record: "+line);
        }
        return fields[index];
    }
}

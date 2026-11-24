package parking.ui;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import parking.exception.InvalidInputException;
import parking.model.FeeType;
import parking.model.VehicleType;
import parking.util.DateTimeUtil;
public final class InputValidator{
    private InputValidator(){
    }
    public static String readPlate(Scanner scanner){
        String line=readLine(scanner);
        if(line.isEmpty()){
            throw new InvalidInputException("Plate number cannot be empty.");
        }
        if(line.length()>12){
            throw new InvalidInputException("Plate number is too long (max 12 characters).");
        }
        if(line.chars().anyMatch(Character::isWhitespace)){
            throw new InvalidInputException("Plate number cannot contain spaces.");
        }
        return line;
    }
    public static int readMenuOption(Scanner scanner,int min,int max){
        String line=readLine(scanner);
        int value;
        try{
            value=Integer.parseInt(line);
        }catch(NumberFormatException e){
            throw new InvalidInputException("Invalid input: please enter a number between "+min+" and "+max+".");
        }
        if(value<min||value>max){
            throw new InvalidInputException("Invalid input: please enter a number between "+min+" and "+max+".");
        }
        return value;
    }
    public static LocalDateTime readDateTime(Scanner scanner){
        String line=readLine(scanner);
        if(line.isEmpty()){
            return LocalDateTime.now().withSecond(0).withNano(0);
        }
        try{
            return LocalDateTime.parse(line,DateTimeUtil.DISPLAY_FORMAT);
        }catch(DateTimeParseException e){
            throw new InvalidInputException("Invalid date time: expected yyyy-MM-dd HH:mm, for example 2026-11-24 14:30.");
        }
    }
    public static VehicleType readVehicleType(Scanner scanner){
        int option=readMenuOption(scanner,1,4);
        if(option==1){
            return VehicleType.CAR;
        }
        if(option==2){
            return VehicleType.MOTORCYCLE;
        }
        if(option==3){
            return VehicleType.TRUCK;
        }
        return VehicleType.ELECTRIC_CAR;
    }
    public static FeeType readFeeType(Scanner scanner){
        int option=readMenuOption(scanner,1,3);
        if(option==1){
            return FeeType.HOURLY;
        }
        if(option==2){
            return FeeType.DAILY;
        }
        return FeeType.MEMBER;
    }
    public static boolean readYesNo(Scanner scanner){
        String line=readLine(scanner);
        return line.equalsIgnoreCase("y")||line.equalsIgnoreCase("yes");
    }
    private static String readLine(Scanner scanner){
        if(!scanner.hasNextLine()){
            throw new InvalidInputException("No more input.");
        }
        return scanner.nextLine().trim();
    }
}

package parking.ui;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import parking.exception.InvalidInputException;
import parking.model.FeeType;
import parking.model.VehicleType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class InputValidatorTest{
    @Test
    void rejectsBlankPlate(){
        assertThrows(InvalidInputException.class,()->InputValidator.readPlate(scanner("\n")));
    }
    @Test
    void rejectsPlateWithSpace(){
        assertThrows(InvalidInputException.class,()->InputValidator.readPlate(scanner("B 12345\n")));
    }
    @Test
    void rejectsPlateLongerThanTwelveCharacters(){
        assertThrows(InvalidInputException.class,()->InputValidator.readPlate(scanner("B123456789012\n")));
    }
    @Test
    void trimsValidPlate(){
        assertEquals("B12345",InputValidator.readPlate(scanner("  B12345  \n")));
    }
    @Test
    void rejectsNonNumericMenuOption(){
        assertThrows(InvalidInputException.class,()->InputValidator.readMenuOption(scanner("abc\n"),1,7));
    }
    @Test
    void rejectsMenuOptionOutOfRange(){
        assertThrows(InvalidInputException.class,()->InputValidator.readMenuOption(scanner("9\n"),1,7));
    }
    @Test
    void rejectsInvalidDateTime(){
        assertThrows(InvalidInputException.class,()->InputValidator.readDateTime(scanner("2026/11/24\n")));
    }
    @Test
    void parsesValidDateTime(){
        assertEquals(LocalDateTime.of(2026,11,24,14,30),InputValidator.readDateTime(scanner("2026-11-24 14:30\n")));
    }
    @Test
    void blankDateTimeFallsBackToNow(){
        LocalDateTime now=LocalDateTime.now();
        LocalDateTime parsed=InputValidator.readDateTime(scanner("\n"));
        assertTrue(Math.abs(Duration.between(now,parsed).toSeconds())<120);
    }
    @Test
    void mapsVehicleTypeOption(){
        assertEquals(VehicleType.CAR,InputValidator.readVehicleType(scanner("1\n")));
        assertEquals(VehicleType.ELECTRIC_CAR,InputValidator.readVehicleType(scanner("4\n")));
    }
    @Test
    void mapsFeeTypeOption(){
        assertEquals(FeeType.HOURLY,InputValidator.readFeeType(scanner("1\n")));
        assertEquals(FeeType.DAILY,InputValidator.readFeeType(scanner("2\n")));
        assertEquals(FeeType.MEMBER,InputValidator.readFeeType(scanner("3\n")));
    }
    @Test
    void readsYesNo(){
        assertTrue(InputValidator.readYesNo(scanner("Y\n")));
        assertTrue(InputValidator.readYesNo(scanner("yes\n")));
        assertFalse(InputValidator.readYesNo(scanner("n\n")));
    }
    private Scanner scanner(String input){
        return new Scanner(input);
    }
}

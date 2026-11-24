package parking.ui;
import java.util.Locale;
import java.util.Scanner;
import parking.exception.InvalidInputException;
import parking.service.PaymentService;
public class ConsolePaymentService implements PaymentService{
    private final Scanner scanner;
    public ConsolePaymentService(Scanner scanner){
        this.scanner=scanner;
    }
    @Override
    public boolean pay(double amount){
        System.out.println("Amount due: "+format(amount)+" HKD");
        System.out.print("Confirm payment? (y/n): ");
        try{
            return InputValidator.readYesNo(scanner);
        }catch(InvalidInputException e){
            return false;
        }
    }
    private String format(double amount){
        return String.format(Locale.ROOT,"%.2f",amount);
    }
}

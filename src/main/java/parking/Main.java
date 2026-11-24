package parking;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import parking.model.CompactSpot;
import parking.model.EvSpot;
import parking.model.LargeSpot;
import parking.model.ParkingSpot;
import parking.policy.FeeStrategyFactory;
import parking.policy.FirstFitSpotAllocationPolicy;
import parking.policy.SpotAllocationPolicy;
import parking.repository.FileParkingRepository;
import parking.repository.ParkingRepository;
import parking.service.ParkingLotService;
import parking.service.PaymentService;
import parking.ui.ConsolePaymentService;
import parking.ui.ConsoleUI;
public class Main{
    private static final int COMPACT_SPOT_COUNT=8;
    private static final int LARGE_SPOT_COUNT=2;
    private static final int EV_SPOT_COUNT=4;
    public static void main(String[] args){
        Path dataDirectory=Path.of(args.length>0?args[0]:"data");
        ParkingRepository repository=new FileParkingRepository(dataDirectory);
        List<ParkingSpot> spots=repository.loadSpots();
        if(spots.isEmpty()){
            spots=createDefaultSpots();
            repository.saveSpots(spots);
            System.out.println("Initialized "+spots.size()+" default spots in "+dataDirectory.toAbsolutePath());
        }
        Scanner scanner=new Scanner(System.in);
        PaymentService paymentService=new ConsolePaymentService(scanner);
        FeeStrategyFactory feeStrategyFactory=new FeeStrategyFactory();
        SpotAllocationPolicy allocationPolicy=new FirstFitSpotAllocationPolicy();
        ParkingLotService service=new ParkingLotService(repository,allocationPolicy,feeStrategyFactory,spots,paymentService);
        new ConsoleUI(service,scanner).start();
    }
    private static List<ParkingSpot> createDefaultSpots(){
        List<ParkingSpot> spots=new ArrayList<>();
        for(int i=1;i<=COMPACT_SPOT_COUNT;i++){
            spots.add(new CompactSpot(String.format("C%02d",i)));
        }
        for(int i=1;i<=LARGE_SPOT_COUNT;i++){
            spots.add(new LargeSpot(String.format("L%02d",i)));
        }
        for(int i=1;i<=EV_SPOT_COUNT;i++){
            spots.add(new EvSpot(String.format("E%02d",i)));
        }
        return spots;
    }
}

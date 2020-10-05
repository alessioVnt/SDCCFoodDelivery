import service.KitchenService;

public class KitchenServiceEntryPoint {

    private KitchenService kitchenService;
    private InitialDataLoader initialDataLoader;

    public KitchenServiceEntryPoint() {
        this.kitchenService = new KitchenService();
        this.initialDataLoader = new InitialDataLoader();
    }

    public void startService(){
        initialDataLoader.createDB();
        kitchenService.kafkaSubscriber();
    }

    public static void main(String[] args){
        KitchenServiceEntryPoint kitchenServiceEntryPoint = new KitchenServiceEntryPoint();
        kitchenServiceEntryPoint.startService();
    }

}



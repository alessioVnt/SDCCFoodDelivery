import service.PaymentService;

public class PaymentServiceEntryPoint {

    private InitialDataLoader initialDataLoader;
    private PaymentService paymentService;

    public PaymentServiceEntryPoint(){
        initialDataLoader = new InitialDataLoader();
        paymentService = new PaymentService();
    }

    public void startService(){
        initialDataLoader.createDB();
        paymentService.kafkaSubscriber();
    }

    public static void main(String[] args){
        PaymentServiceEntryPoint paymentServiceEntryPoint = new PaymentServiceEntryPoint();
        paymentServiceEntryPoint.startService();
    }
}

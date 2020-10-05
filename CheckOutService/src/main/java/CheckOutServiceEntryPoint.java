import chekoutKafkaTools.CheckoutKafkaConsumer;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.log4j.BasicConfigurator;
import sagaOrchestrator.SagaOrchestrator;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

//Init class for the whole service
public class CheckOutServiceEntryPoint {

    private static final Logger logger = Logger.getLogger(CheckOutServiceEntryPoint.class.getName());

    private Server server;

    private void startServer() throws IOException {
        /* The port on which the server should run */
        //BasicConfigurator.configure();
        int port = 5050;
        port = Integer.parseInt(System.getenv("PORT"));
        server = ServerBuilder.forPort(port)
                .addService(new CheckOutServiceGrpcImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    CheckOutServiceEntryPoint.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

/*      //Test purpose only delete this
        MenuItem itemToAdd = new MenuItem("pollo", 12, 1.99f);
        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(itemToAdd);

        NewOrderMessage newOrderMessage = new NewOrderMessage("Alessio", "RistoranteProva",menuItems , "4080", "123", "12/22");
        newOrderMessage.setTransactionID("1");
        Gson gson = new Gson();

        System.out.println(gson.toJson(newOrderMessage));*/

        //Initialize Hbase

        //Initialize Saga Orchestrator on a different thread
        System.out.println("Launching Saga orchestrator thread");
        SagaOrchestrator sagaOrchestrator = new SagaOrchestrator();
        new Thread(sagaOrchestrator).start();

        //Initialize KafkaConsumer thread for the CheckoutService
        System.out.println("Launching KafkaConsumer thread");
        CheckoutKafkaConsumer checkoutKafkaConsumer = new CheckoutKafkaConsumer();
        new Thread(checkoutKafkaConsumer).start();

        //Start GRPC server
        System.out.println("Starting Grpc Server");
        final CheckOutServiceEntryPoint checkOutServiceEntryPoint = new CheckOutServiceEntryPoint();
        checkOutServiceEntryPoint.startServer();
        checkOutServiceEntryPoint.blockUntilShutdown();



    }
}

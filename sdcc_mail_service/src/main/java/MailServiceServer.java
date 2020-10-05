import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import service.MailService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import sdccFoodDelivery.*;
import service.MailServiceInterface;

import static java.lang.Boolean.TRUE;

public class MailServiceServer {

    private static final Logger logger = Logger.getLogger(MailServiceServer.class.getName());

    private Server server;
    private InitialDataLoader initialDataLoader;

    public MailServiceServer() {
        this.initialDataLoader = new InitialDataLoader();
    }
    
    private void initDB(){
        try {
            initialDataLoader.createDB();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 9555;
        port = Integer.parseInt(System.getenv("PORT"));
        server = ServerBuilder.forPort(port)
                .addService(new MailServiceServer.MailServiceImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    MailServiceServer.this.stop();
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
        final MailServiceServer server = new MailServiceServer();
        server.initDB();
        server.start();
        server.blockUntilShutdown();
    }

    static class MailServiceImpl extends sdcc_mail_serviceGrpc.sdcc_mail_serviceImplBase{

        private static MailServiceInterface mailService = new MailService();

        @Override
        public void sendMail(SendMailRequest request, StreamObserver<BooleanMessage> responseObserver) {
            System.out.println("SendMail function init");
            boolean isSuccessful = mailService.SendMail(request.getTag(), Integer.parseInt(request.getUserID()));
            System.out.println("isSuccessful: " + isSuccessful);
            BooleanMessage response = BooleanMessage.newBuilder().setOk(isSuccessful).build();
            System.out.println("response: " + response.getOk());
            responseObserver.onNext(response);
            System.out.println("Response sended");
            responseObserver.onCompleted();
        }
    }
}

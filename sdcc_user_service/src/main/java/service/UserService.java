package service;

import controller.UserController;
import dataSource.InitialDataLoader;
import encoder.Encoder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import model.PaymentMethod;
import model.User;
import sdccFoodDelivery.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.lang.Boolean.FALSE;

public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    private Server server;
    private InitialDataLoader initialDataLoader;

    public UserService() {
        this.initialDataLoader = new InitialDataLoader();
    }

    public void initDB(){
        try {
            initialDataLoader.createDB();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 3550;
        port = Integer.parseInt(System.getenv("PORT"));
        server = ServerBuilder.forPort(port)
                .addService(new UserServiceImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    UserService.this.stop();
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
        final UserService server = new UserService();
        server.initDB();
        server.start();
        server.blockUntilShutdown();
    }

    static class UserServiceImpl extends sdcc_user_serviceGrpc.sdcc_user_serviceImplBase {

        private UserController userController;
        //Encoder is used to hash the password and credit cards data in order
        //to not store them clearly
        private Encoder encoder;

        public UserServiceImpl(){
            this.userController = new UserController();
            this.encoder = new Encoder();
        }

        //Save a new user
        @Override
        public void save(UserInfoMessage request, StreamObserver<BooleanMessage> responseObserver) {
            PaymentMethod paymentMethod = new PaymentMethod(this.encoder.encode(request.getCreditCardNumber()),
                    this.encoder.encode(request.getDeadline()),this.encoder.encode(request.getThreeDigitCode()));
            boolean controlOperation =  this.userController.save(new User(request.getUsername(),
                    this.encoder.encode(request.getPassword()), request.getAddress(), request.getMail(), paymentMethod));

            BooleanMessage response = BooleanMessage.newBuilder().setOk(controlOperation).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();


        }

        //Delete user by ID
        @Override
        public void deleteByID(IDMessage request, StreamObserver<BooleanMessage> responseObserver) {
            boolean controlOperation =  this.userController.deleteByID((request.getId()));
            BooleanMessage response = BooleanMessage.newBuilder().setOk(controlOperation).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        //Log in
        @Override
        public void logIn(LogInMessage request, StreamObserver<LogInResponse> responseObserver){
            User user = this.userController.findByMail(request.getMail());
            //No user found or wrong password
            if (user == null || !this.encoder.encode(request.getPassword()).equals(user.getPassword())){
                if (user != null)System.out.println("User password: " + user.getPassword());
                LogInResponse response = LogInResponse.newBuilder().
                        setLogged(false).
                        build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {//User found
                LogInResponse response = LogInResponse.newBuilder().
                        setLogged(true).
                        setUserId(user.getID()).
                        setUsername(user.getUsername()).
                        build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }

        //Find user by ID
        @Override
        public void findByID(IDMessage request, StreamObserver<UserMessage> responseObserver) {
           System.out.println("ID dell'utente cercato:  " + request.getId());
           User user =  this.userController.findByID(request.getId());
           System.out.println(user.getAddress() + " " + user.getPreferiti());
           String preferred = user.getPreferiti().toString();
           PreferitiMessage preferitiMessage = PreferitiMessage.newBuilder().setPreferito(user.getPreferiti()[0]).build();
           CreditCard creditCardMessage = CreditCard.newBuilder().setCreditCardNumber(user.getPaymentMethod().getCreditCards().get(0).getCreditCardNumber()).
                   setDeadLine(user.getPaymentMethod().getCreditCards().get(0).getDeadLine()).setThreeDigitCode(user.getPaymentMethod().getCreditCards().get(0).getThreeDigitCode()).build();
           UserMessage response = UserMessage.newBuilder().setId(user.getID()).setUsername(user.getUsername()).
            setPassword(user.getPassword()).setAddress(user.getAddress()).setMail(user.getMail()).setCreditCard(creditCardMessage)
                   .setPreferito(preferitiMessage).build();
           responseObserver.onNext(response);
           responseObserver.onCompleted();
        }

        //Update an existing user
        private boolean modifyUser(User newUser){
            return this.userController.updateUser(newUser);
        }

        //Update an existing user
        @Override
        public void updateUsername(UpdateUsernameMessage request, StreamObserver<BooleanMessage> responseObserver) {
            User newUser;
            if((newUser = this.userController.findByID(request.getId())) == null){
                BooleanMessage response = BooleanMessage.newBuilder().setOk(FALSE).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
            newUser.setUsername(request.getNewUsername());
            boolean controlOperation = this.modifyUser(newUser);
            BooleanMessage response = BooleanMessage.newBuilder().setOk(controlOperation).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void updatePassword(UpdatePasswordMessage request, StreamObserver<BooleanMessage> responseObserver) {
            User newUser;
            if((newUser = this.userController.findByID(request.getId())) == null){
                BooleanMessage response = BooleanMessage.newBuilder().setOk(FALSE).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
            newUser.setPassword(this.encoder.encode(request.getNewPassword()));
            boolean controlOperation = this.modifyUser(newUser);
            BooleanMessage response = BooleanMessage.newBuilder().setOk(controlOperation).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void updateAddress(UpdateAddressMessage request, StreamObserver<BooleanMessage> responseObserver) {
            User newUser;
            if((newUser = this.userController.findByID(request.getId())) == null){
                BooleanMessage response = BooleanMessage.newBuilder().setOk(FALSE).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
            newUser.setAddress(request.getNewAddress());
            boolean controlOperation = this.modifyUser(newUser);
            BooleanMessage response = BooleanMessage.newBuilder().setOk(controlOperation).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void updateMail(UpdateMailMessage request, StreamObserver<BooleanMessage> responseObserver) {
            User newUser;
            if((newUser = this.userController.findByID(request.getId())) == null){
                BooleanMessage response = BooleanMessage.newBuilder().setOk(FALSE).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
            newUser.setMail(request.getNewMail());
            boolean controlOperation = this.modifyUser(newUser);
            BooleanMessage response = BooleanMessage.newBuilder().setOk(controlOperation).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void updatePaymentMethod(UpdatePaymentMessage request, StreamObserver<BooleanMessage> responseObserver) {
            User newUser;
            if((newUser = this.userController.findByID(request.getId())) == null) {
                BooleanMessage response = BooleanMessage.newBuilder().setOk(FALSE).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
            PaymentMethod newPaymentMethod = newUser.getPaymentMethod();
            newPaymentMethod.addNewPaymentMethod(this.encoder.encode(request.getCreditCard().getCreditCardNumber()),
                    this.encoder.encode(request.getCreditCard().getDeadLine()),this.encoder.encode(request.getCreditCard().getThreeDigitCode()));
            newUser.setPaymentMethod(newPaymentMethod);
            boolean controlOperation = this.modifyUser(newUser);
            BooleanMessage response = BooleanMessage.newBuilder().setOk(controlOperation).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void updatePreferiti(UpdatePreferitiMessage request, StreamObserver<BooleanMessage> responseObserver) {
            User newUser;
            if((newUser = this.userController.findByID(request.getId())) == null){
                BooleanMessage response = BooleanMessage.newBuilder().setOk(FALSE).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
            //Update preferiti array, deleting latest element
            String[] array = newUser.getPreferiti();
            for (int I = newUser.getPreferiti().length - 1; I >= 1; I--) array[I] = array[I-1];
            System.out.println("Nuovo preferito letto nella richiesta: " + request.getNewPreferito());
            array[0] = request.getNewPreferito();
            System.out.println("Array dei nuovi preferiti da inserire nell'utente 0: " + array[0] + " 1: " + array[1] + " 2: " + array[2] );

            newUser.setPreferiti(array);
            System.out.println("Preferiti Inseriti nell'utente: " + newUser.getPreferiti().toString());
            boolean controlOperation = this.modifyUser(newUser);
            BooleanMessage response = BooleanMessage.newBuilder().setOk(controlOperation).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getUserPreferitiByID(IDMessage request, StreamObserver<PreferitiMessage> responseObserver) {
            User newUser;
            if((newUser = this.userController.findByID(request.getId())) == null) {
                PreferitiMessage response = PreferitiMessage.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
            String[] array = newUser.getPreferiti();
            int size = 0;

            for (int I = 0; I < newUser.getPreferiti().length; I++){
                if (!array[I].equals("0")) size++;
                else break;
            }
            String[] preferiti = new String[size];
            System.arraycopy(array, 0, preferiti, 0, size);
            for (int i = 0; i < preferiti.length; i++){
                String pref = preferiti[i].replaceAll("\\u0027", "'");
                System.out.println("Preferito da mandare in risposta: " + pref);
                PreferitiMessage response = PreferitiMessage.newBuilder().setPreferito(pref).build();
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        }
    }
  /*  private UserController userController;
    //Encoder is used to hash the password and credit cards data in order
    //to not store them clearly
    private Encoder encoder;

    public UserService(){
        this.userController = new UserController();
        this.encoder = new Encoder();
    }

    //Save a new user
    public boolean save(String username, String password, String address, String mail, String creditCardNumber, String deadline, String threeDigitCode){
        PaymentMethod paymentMethod = new PaymentMethod(this.encoder.encode(creditCardNumber),this.encoder.encode(deadline),this.encoder.encode(threeDigitCode));
        return this.userController.save(new User(username, this.encoder.encode(password), address, mail, paymentMethod));
    }

    //Delete user by ID
    public boolean deleteByID(int id){
        return this.userController.deleteByID(id);
    }

    //Find user by ID
    public User findByID(int id){
        return this.userController.findByID(id);
    }

    //Update an existing user
    private boolean modifyUser(User newUser){
        if(!this.userController.deleteByID(newUser.getID())) return false;
        return this.userController.save(newUser);
    }

    //Update user's username
    public boolean updateUsername(String newUsername, int id){
        User newUser;
        if((newUser = this.userController.findByID(id)) == null) return false;
        newUser.setUsername(newUsername);
        return this.modifyUser(newUser);
    }

    //Update user's password
    public boolean updatePassword(String newPassword, int id){
        User newUser;
        if((newUser = this.userController.findByID(id)) == null) return false;
        newUser.setPassword(newPassword);
        return this.modifyUser(newUser);
    }

    //Update user's address
    public boolean updateAddress(String newAddress, int id){
        User newUser;
        if((newUser = this.userController.findByID(id)) == null) return false;
        newUser.setAddress(newAddress);
        return this.modifyUser(newUser);
    }

    //Update user's mail
    public boolean updateMail(String newMail, int id){
        User newUser;
        if((newUser = this.userController.findByID(id)) == null) return false;
        newUser.setMail(newMail);
        return this.modifyUser(newUser);
    }

    //Update payment method
    public boolean updatePaymentMethod(String creditCardNumber, String deadline, String threeDigitCode, int id){
        User newUser;
        if((newUser = this.userController.findByID(id)) == null) return false;
        PaymentMethod newPaymentMethod = newUser.getPaymentMethod();
        newPaymentMethod.addNewPaymentMethod(this.encoder.encode(creditCardNumber),this.encoder.encode(deadline),this.encoder.encode(threeDigitCode));
        newUser.setPaymentMethod(newPaymentMethod);
        return this.modifyUser(newUser);
    }

    public boolean updatePreferiti(String newPreferito, int id) {
        User newUser;
        if((newUser = this.userController.findByID(id)) == null) return false;

        //Update preferiti array, deleting latest element
        String[] array = newUser.getPreferiti();
        for (int I = newUser.getPreferiti().length - 1; I >= 1; I--) array[I] = array[I-1];
        array[0] = newPreferito;

        newUser.setPreferiti(array);
        return this.modifyUser(newUser);
    }

    public String[] getUserPreferitiByID(int id) {
        User newUser;
        if((newUser = this.userController.findByID(id)) == null) return null;
        String[] array = newUser.getPreferiti();
        int size = 0;

        for (int I = 0; I < newUser.getPreferiti().length; I++){
            if (!array[I].equals("0")) size++;
            else break;
        }
        String[] preferiti = new String[size];
        System.arraycopy(array, 0, preferiti, 0, size);
        return preferiti;
    }*/


}

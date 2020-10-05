import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import sdccFoodDelivery.*;

public class UserServiceClient {

    final  sdcc_user_serviceGrpc.sdcc_user_serviceBlockingStub blockingStub;


    public UserServiceClient(Channel channel) {
        this.blockingStub = sdcc_user_serviceGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC client");
        final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        final UserServiceClient client = new UserServiceClient(channel);
        client.run(channel);
    }

    private void run(ManagedChannel channel) {

        //scrivere metodo da chiamare

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void save(String username, String password, String address, String mail, String creditCardNumber, String deadline,
                      String threeDigitCode){

        final UserInfoMessage userInfoMessage = UserInfoMessage.newBuilder().setUsername(username).setPassword(password).
                setAddress(address).setMail(mail).setCreditCardNumber(creditCardNumber).setDeadline(deadline).
                setThreeDigitCode(threeDigitCode).build();

        BooleanMessage response;
        try{
            response = blockingStub.save(userInfoMessage);
        } catch (StatusRuntimeException e){
            e.printStackTrace();
            return;
        }
        if(response.getOk()){
            System.out.println("Information correctly saved");
        }else {
            System.out.println("Information unsaved");
        }
    }

    private void deleteByID(int id){
        final IDMessage IdMessage = IDMessage.newBuilder().setId(id).build();
        BooleanMessage response;
        try{
            response = blockingStub.deleteByID(IdMessage);
        } catch (StatusRuntimeException e){
            e.printStackTrace();
            return;
        }
        if(response.getOk()){
            System.out.println("Correctly deleted");
        }else {
            System.out.println("Uncorrectly deleted");
        }
    }

    private void findByID(int id){
        final IDMessage idMessage = IDMessage.newBuilder().setId(id).build();

        UserMessage response;

        try {
            response = blockingStub.findByID(idMessage);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Found User!\n" + "id: " + response.getId() +
                "\nUsername: " + response.getUsername() + "\nPassword: " + response.getPassword() +
                "\nAddress: " + response.getAddress() + "\nMail: " + response.getMail()+ "\nCreditCard: "+ response.getCreditCard() +
                "\nPreferito: "+ response.getPreferito());
    }

    private void updateUsername(String newUsername, int id){
        final UpdateUsernameMessage updateUsernameMessage = UpdateUsernameMessage.newBuilder().setId(id).setNewUsername(newUsername)
                .build();
        BooleanMessage response;
        try{
            response = blockingStub.updateUsername(updateUsernameMessage);
        } catch (StatusRuntimeException e){
            e.printStackTrace();
            return;
        }
        if(response.getOk()){
            System.out.println("Correctly updated");
        }else {
            System.out.println("Uncorrectly updated");
        }
    }

    private void updatePassword(String newPassword, int id) {
        final UpdatePasswordMessage updatePasswordMessage = UpdatePasswordMessage.newBuilder().setNewPassword(newPassword).setId(id)
                .build();
        BooleanMessage response;
        try {
            response = blockingStub.updatePassword(updatePasswordMessage);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return;
        }
        if (response.getOk()) {
            System.out.println("Correctly updated");
        } else {
            System.out.println("Uncorrectly updated");
        }
    }

    private void updateAddress(String newAddress, int id){
            final UpdateAddressMessage updateAddressMessage = UpdateAddressMessage.newBuilder().setNewAddress(newAddress).setId(id)
                    .build();
            BooleanMessage response;
            try{
                response = blockingStub.updateAddress(updateAddressMessage);
            } catch (StatusRuntimeException e){
                e.printStackTrace();
                return;
            }
            if(response.getOk()){
                System.out.println("Correctly updated");
            }else {
                System.out.println("Uncorrectly updated");
            }
    }

    private void updateMail(String newMail, int id){
        final UpdateMailMessage updateMailMessage = UpdateMailMessage.newBuilder().setNewMail(newMail).setId(id).build();
        BooleanMessage response;
        try{
            response = blockingStub.updateMail(updateMailMessage);
        } catch (StatusRuntimeException e){
            e.printStackTrace();
            return;
        }
        if(response.getOk()){
            System.out.println("Correctly updated");
        }else {
            System.out.println("Uncorrectly updated");
        }
    }

    private void updatePaymentMethod(String creditCardNumber, String deadline, String threeDigitCode, int id){
        final UpdatePaymentMessage updatePaymentMessage = UpdatePaymentMessage.newBuilder().
                setCreditCard(CreditCard.newBuilder().setCreditCardNumber(creditCardNumber).setDeadLine(deadline).setThreeDigitCode(threeDigitCode).build())
                .setId(id).build();
        BooleanMessage response;
        try{
            response = blockingStub.updatePaymentMethod(updatePaymentMessage);
        } catch (StatusRuntimeException e){
            e.printStackTrace();
            return;
        }
        if(response.getOk()){
            System.out.println("Correctly updated");
        }else {
            System.out.println("Uncorrectly updated");
        }
    }

    private void updatePreferiti(String newPreferito, int id){
        final UpdatePreferitiMessage updatePreferitiMessage = UpdatePreferitiMessage.newBuilder().setNewPreferito(newPreferito)
                .setId(id).build();
        BooleanMessage response;
        try{
            response = blockingStub.updatePreferiti(updatePreferitiMessage);
        } catch (StatusRuntimeException e){
            e.printStackTrace();
            return;
        }
        if(response.getOk()){
            System.out.println("Correctly updated");
        }else {
            System.out.println("Uncorrectly updated");
        }
    }

    private void getUserPreferitiByID(int id) {
        final IDMessage idMessage = IDMessage.newBuilder().setId(id).build();

        blockingStub.getUserPreferitiByID(idMessage)
                .forEachRemaining(PreferitiMessage -> {
                    System.out.println("Founded Preferiti: \n" +
                            " Preferito: " + PreferitiMessage.getPreferito());
                });

    }

}

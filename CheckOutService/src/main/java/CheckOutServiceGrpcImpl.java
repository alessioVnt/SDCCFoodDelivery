
import io.grpc.stub.StreamObserver;
import sdccFoodDelivery.MenuItem;
import sdccFoodDelivery.TransactionInfo;
import sdccFoodDelivery.TransactionOutcome;
import sdccFoodDelivery.checkoutServiceGrpc;

import java.util.ArrayList;
import java.util.List;

//GRPC Server implementation class
public class CheckOutServiceGrpcImpl extends  checkoutServiceGrpc.checkoutServiceImplBase{

    private final CheckOutServiceController checkOutServ = new CheckOutServiceController();
    private int numberOfRequests = 0;

    @Override
    public void executeTransaction(TransactionInfo request, StreamObserver<TransactionOutcome> responseObserver) {
        //Testing purpose counter
        numberOfRequests++;

        System.out.println("message arrived");
        List<entities.MenuItem> menuItems = new ArrayList<>();
        for (MenuItem menuItem: request.getItemsList()) {
            menuItems.add(new entities.MenuItem(menuItem.getProductId(), menuItem.getQuantity(), menuItem.getPrice()));
        }
        boolean isTransactionSuccessful = checkOutServ.initiateCheckout(request.getUserID(), request.getRestaurantID(), menuItems, request.getCardNumber(), request.getCvc(), request.getCardExpiration());

        TransactionOutcome response = TransactionOutcome.newBuilder().setIsSuccessful(isTransactionSuccessful).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
}


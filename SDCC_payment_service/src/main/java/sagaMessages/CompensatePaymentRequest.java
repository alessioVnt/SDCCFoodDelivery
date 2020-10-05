package sagaMessages;

import java.util.UUID;

public class CompensatePaymentRequest {
    public String userID;
    public UUID transactionID;
    public float totalPrice;

    public CompensatePaymentRequest(String userID, UUID transactionID, float totalPrice) {
        this.userID = userID;
        this.transactionID = transactionID;
        this.totalPrice = totalPrice;
    }
}

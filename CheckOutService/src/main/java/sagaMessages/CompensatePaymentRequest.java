package sagaMessages;

public class CompensatePaymentRequest {
    public String userID;
    public String transactionID;
    public float totalPrice;

    public CompensatePaymentRequest(String userID, String transactionID, float totalPrice) {
        this.userID = userID;
        this.transactionID = transactionID;
        this.totalPrice = totalPrice;
    }
}

package sagaMessages;

public class PaymentRequest {

    public String userID;
    public String cardNumber;
    public String cvc;
    public String cardExpireDate;
    public String transactionID;
    public float totalPrice;

    public PaymentRequest(String userID, String cardNumber, String cvc, String cardExpireDate, float totalPrice, String transactionID) {
        this.userID = userID;
        this.cardNumber = cardNumber;
        this.cvc = cvc;
        this.cardExpireDate = cardExpireDate;
        this.transactionID = transactionID;
        this.totalPrice = totalPrice;
    }
}

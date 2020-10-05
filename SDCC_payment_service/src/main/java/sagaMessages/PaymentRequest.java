package sagaMessages;

import java.util.UUID;

public class PaymentRequest {

    public String userID;
    public String cardNumber;
    public String cvc;
    public String cardExpireDate;
    public UUID transactionID;
    public float totalPrice;

    public PaymentRequest(String userID, String cardNumber, String cvc, String cardExpireDate, float totalPrice, UUID transactionID) {
        this.userID = userID;
        this.cardNumber = cardNumber;
        this.cvc = cvc;
        this.cardExpireDate = cardExpireDate;
        this.transactionID = transactionID;
        this.totalPrice = totalPrice;
    }
}

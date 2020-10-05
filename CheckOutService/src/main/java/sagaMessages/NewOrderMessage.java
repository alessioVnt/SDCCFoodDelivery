package sagaMessages;

import entities.MenuItem;

import java.util.List;

public class NewOrderMessage {

    public String userID;
    public String restaurantID;
    public List<MenuItem> menuItemList;
    public float totalPrice;
    public String cardNumber;
    public String cvc;
    public String cardExpireDate;
    public String transactionID;

    public NewOrderMessage(String userID, String restaurantID, List<MenuItem> menuItemList, String cardNumber, String cvc, String cardExpireDate) {
        this.userID = userID;
        this.restaurantID = restaurantID;
        this.menuItemList = menuItemList;
        this.cardNumber = cardNumber;
        this.cvc = cvc;
        this.cardExpireDate = cardExpireDate;
        calculateTotal();
    }

    public void NewOrderMessage(String jSonMessage){
        //Parse Json
        calculateTotal();
    }

    private void calculateTotal(){
        for (MenuItem menuItem: menuItemList) {
            totalPrice += menuItem.price * menuItem.quantity;
        }
    }

    public void setTransactionID(String transactionID){
        this.transactionID = transactionID;
    }
}

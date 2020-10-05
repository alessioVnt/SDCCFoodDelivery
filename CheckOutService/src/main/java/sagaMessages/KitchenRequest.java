package sagaMessages;

import entities.MenuItem;

import java.util.List;

public class KitchenRequest {
    public String restaurantID;
    public List<MenuItem> menuItems;
    public float totalPrice;
    public String transactionID;

    public KitchenRequest(String restaurantID, List<MenuItem> menuItems, float totalPrice, String transactionID) {
        this.restaurantID = restaurantID;
        this.menuItems = menuItems;
        this.totalPrice = totalPrice;
        this.transactionID = transactionID;
    }
}

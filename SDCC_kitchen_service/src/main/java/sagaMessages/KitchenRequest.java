package sagaMessages;

import model.MenuItem;

import java.util.List;
import java.util.UUID;

public class KitchenRequest {
    public String restaurantID;
    public List<MenuItem> menuItems;
    public float totalPrice;
    public UUID transactionID;

    public KitchenRequest(String restaurantID, List<MenuItem> menuItems, float totalPrice, UUID transactionID) {
        this.restaurantID = restaurantID;
        this.menuItems = menuItems;
        this.totalPrice = totalPrice;
        this.transactionID = transactionID;
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public UUID getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(UUID transactionID) {
        this.transactionID = transactionID;
    }
}

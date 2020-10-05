package model;

import com.datastax.driver.core.utils.UUIDs;

import java.util.List;
import java.util.UUID;

//Model class
public class KitchenEvent {

    private UUID eventID;
    private UUID transactionID;
    private float price;
    private List<MenuItem> menuItems;

    //Constructor: new event occurred
    public KitchenEvent(UUID transactionID, float price, List<MenuItem> menuItems){
        this.eventID = UUIDs.timeBased();
        this.transactionID = transactionID;
        this.price = price;
        this.menuItems = menuItems;
    }

    public UUID getEventID() {
        return eventID;
    }

    public void setEventID(UUID eventID) {
        this.eventID = eventID;
    }

    public UUID getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(UUID transactionID) {
        this.transactionID = transactionID;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }
}

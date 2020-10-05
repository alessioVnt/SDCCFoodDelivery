package model;

import com.datastax.driver.core.utils.UUIDs;

import java.util.UUID;

//Entity paymentEvent
public class PaymentEvent {

    private UUID eventId;
    private UUID transactionId;
    private String status;
    private String userId;
    private float price;

    //New event from scratch occurred
    public PaymentEvent(String userId, float price){
        this.eventId = UUIDs.timeBased();
        this.transactionId = UUIDs.timeBased();
        this.status = "";
        this.userId = userId;
        this.price = price;
    }

    //New event due to aborted or rejected payment transaction occurred
    public PaymentEvent(UUID transactionId, String userId, float price){
        this.eventId = UUIDs.timeBased();
        this.transactionId = transactionId;
        this.status = "";
        this.userId = userId;
        this.price = price;
    }

    //Event from find query to DB
    public PaymentEvent(UUID event_id, UUID payment_id, String status, String user_id, float price) {
        this.eventId = event_id;
        this.transactionId = payment_id;
        this.status = status;
        this.userId = user_id;
        this.price = price;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}

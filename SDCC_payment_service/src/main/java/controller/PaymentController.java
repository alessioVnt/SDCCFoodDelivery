package controller;

import com.datastax.driver.core.ResultSet;
import dataSource.DataSource;
import model.PaymentEvent;

import java.util.UUID;

//Payment event Controller
public class PaymentController {

    public PaymentController(){
        super();
    }

    //Save a new record to DB
    public void save(PaymentEvent paymentEvent){
        String query = "INSERT INTO " +
                "sdcc_payment_event_db.payment_event" + " (event_id, transaction_id, status, user_id, price)\n " +
                "VALUES (" + paymentEvent.getEventId().toString() +
                ", " + paymentEvent.getTransactionId().toString() +
                ", '" + paymentEvent.getStatus() +
                "' , '" + paymentEvent.getUserId() +
                "' , " + paymentEvent.getPrice() +
                ") ;\n";
        System.out.println(query);
        DataSource dataSource = new DataSource();
        dataSource.getSession().execute(query);
    }

    //Find an event by its ID
    public PaymentEvent findById(UUID transactionId) {
        String query = "SELECT * FROM payment_event WHERE transaction_id ='" + transactionId +"';";
        DataSource dataSource = new DataSource();
        final ResultSet resultSet = dataSource.getSession().execute(query);
        final PaymentEvent[] paymentEvent = new PaymentEvent[1];
        resultSet.forEach(r -> paymentEvent[0] = new PaymentEvent(r.getUUID("event_id"), r.getUUID("transaction_id"),
                r.getString("status"), r.getString("user_id"), r.getFloat("price")));
        return paymentEvent[0];
    }
}

package controller;

import com.google.gson.Gson;
import dataSource.DataSource;
import model.KitchenEvent;

//Payment event Controller
public class KitchenController {

    public KitchenController(){
        super();
    }

    //Save a new record to DB
    public void save(KitchenEvent kitchenEvent){
        Gson gson = new Gson();
        String query = "INSERT INTO " +
                "sdcc_kitchen_event_db.kitchen_event" + " (event_id, transaction_id, price, menu_items) " +
                "VALUES (" + kitchenEvent.getEventID().toString() +
                ", " + kitchenEvent.getTransactionID().toString() +
                ", " + kitchenEvent.getPrice() +
                ", '" + gson.toJson(kitchenEvent.getMenuItems()) +
                "' );";
        DataSource dataSource = new DataSource();
        dataSource.getSession().execute(query);
    }
}

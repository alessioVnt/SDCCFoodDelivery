package interfaces;

import CassandraEventStore.CassandraStoreImpl;
import constants.StateEnum;
import entities.MenuItem;

import java.util.List;

public interface ICassandraStore {

    //Initialize DB and connect
    public void initialize();


    //CRUD

    //If transactionID == "" create a transactionID
    public String putEvent(String userID, String restaurantID, List<MenuItem> menuItemList, float totalPrice, StateEnum state, String transactionID);
}

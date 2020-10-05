package CassandraEventStore;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.UUIDs;
import com.google.gson.Gson;
import constants.StateEnum;
import entities.MenuItem;
import interfaces.ICassandraStore;

import java.util.List;
import java.util.UUID;

public class CassandraStoreImpl implements ICassandraStore {
    private static Cluster cluster;

    private static Session session = null;

    private String ROOT_ADDRESS = "cassandra";
    private Integer ROOT_PORT = 9042;
    private String KEYSPACE_NAME = "sdcc_checkout_event_db";
    private String TABLE_NAME = "checkout_event";


    public void initialize() {
        ensureCassandraIsConnected();
        createKeyspace();
        createTable();
    }

    private void ensureCassandraIsConnected() {
        if (session == null){
            Cluster.Builder b = Cluster.builder().addContactPoint(this.ROOT_ADDRESS);
            if (this.ROOT_PORT != null) {
                b.withPort(this.ROOT_PORT);
            }
            cluster = b.build();
            session = cluster.connect();
        }
    }

    private void createKeyspace() {
        String query = "CREATE KEYSPACE IF NOT EXISTS " +
                this.KEYSPACE_NAME + " WITH replication = {" +
                "'class':'" + "SimpleStrategy" +
                "','replication_factor':" + 1 +
                "};";
        session.execute(query);
    }

    private void createTable() {
        String query =
                " CREATE TABLE IF NOT EXISTS " +
                        this.KEYSPACE_NAME + "." +
                        this.TABLE_NAME + " (" +
                        "event_id timeuuid PRIMARY KEY, " +
                        "transaction_id timeuuid," +
                        "status text," +
                        "user_id text," +
                        "restaurant_id text," +
                        "order_list text," +
                        "price float);";
        session.execute(query);
    }

    //If transactionID == "" create a transactionID
    public String putEvent(String userID, String restaurantID, List<MenuItem>  menuItemList, float totalPrice, StateEnum state, String transactionID) {

        ensureCassandraIsConnected();

        Gson gson = new Gson();
        String orderList = gson.toJson(menuItemList);

        String transactionUUID = transactionID;
        if (transactionUUID == null || transactionUUID.equals("")) transactionUUID = UUIDs.timeBased().toString();

        String query = "INSERT INTO " +
                "sdcc_checkout_event_db.checkout_event" + " (event_id, transaction_id, status, user_id, restaurant_id, order_list, price)\n " +
                "VALUES (" + UUIDs.timeBased().toString() +
                ", " + transactionUUID.toString() +
                ", '" + state.toString() +
                "' , '" + userID +
                "' , '" + restaurantID +
                "' , '" + orderList +
                "' , " + totalPrice +
                ") ;\n";
        System.out.println(query);
        session.execute(query);

        return transactionUUID;
    }
}

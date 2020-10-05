package interfaces;

import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;
import java.util.ArrayList;

public interface IHBaseStore {

    //Initialize DB and connect
    public void initialize();  //viene inizializzato in connectiontrial

    //HBase CRUD
    public void putRowToTable(String userId, String restaurantId, String order, Double price) throws IOException;

    public ArrayList<Double> getPriceContent() throws IOException;

    public boolean nameSpaceExists(Admin admin, String namespaceName) throws IOException;
}

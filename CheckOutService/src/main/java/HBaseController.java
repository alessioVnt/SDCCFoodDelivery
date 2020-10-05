import com.google.protobuf.ServiceException;
import hBaseCheckOutStore.HBaseStoreImpl;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

import java.io.IOException;
import java.util.ArrayList;

public class HBaseController {

    static final TableName TABLE_NAME = TableName.valueOf("OrderTable");
    HBaseStoreImpl hBaseStore = new HBaseStoreImpl();

    public Connection connectionTrial() throws IOException {
        Configuration config = HBaseConfiguration.create();

        String path = "./hbase-site.xml";
        config.addResource(new Path(path));

        /*try (Connection connection = ConnectionFactory.createConnection(config);
             Admin admin = connection.getAdmin()){

            admin.getClusterStatus(); // assure connection successfully established
            System.out.println("\n*** Hello HBase! -- Connection has been "
                    + "established via Zookeeper!!\n");

            HBaseStoreImpl hBaseStore = new HBaseStoreImpl();
            hBaseStore.createNamespaceAndTable(admin);

            Table table = connection.getTable(TableName.valueOf("OrderTable"));

            hBaseStore.putRowToTable(table);
        }*/

        try (Connection connection = ConnectionFactory.createConnection();
             Admin admin = connection.getAdmin()) {


            admin.getClusterStatus(); // assure connection successfully established
            System.out.println("\n*** Hello HBase! -- Connection has been "
                    + "established via Zookeeper!!\n");

            hBaseStore.createNamespaceAndTable(admin);

            //Table table = connection.getTable(TABLE_NAME);

            return connection;
        }

    }

    public Double getMeanOutgoingFromUser(String userId) throws IOException {
        return hBaseStore.getMeanOutgoingFromUser(userId);
    }

    public Double getMeanOutgoing() throws IOException{
        return hBaseStore.getMeanOutgoing();
    }

    public ArrayList<Double> getPriceContent() throws IOException {
        return hBaseStore.getPriceContent();
    }
}

